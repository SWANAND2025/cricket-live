package com.example.cricket;

import jakarta.annotation.PostConstruct;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MatchService {
    private static final String[] TEAMS = {"India", "Australia", "England", "Pakistan", "South Africa", "New Zealand", "Sri Lanka", "West Indies"};
    private final Map<String, Match> matches = new ConcurrentHashMap<>();
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final AtomicInteger seq = new AtomicInteger();
    private final Random rnd = new Random();

    @PostConstruct
    void init() {
        createMatch("India", "Australia", 10);
        createMatch("England", "Pakistan", 10);
    }

    public Match createMatch(String a, String b, int overs) {
        Match m = new Match();
        m.no = seq.incrementAndGet();
        m.id = "M" + m.no;
        m.teamA = new Team(a);
        m.teamB = new Team(b);
        m.maxOvers = Math.max(1, Math.min(20, overs));
        m.title = a + " vs " + b + " (" + m.maxOvers + " overs)";
        m.log(a + " won the toss and elected to bat");
        matches.put(m.id, m);
        return m;
    }

    public List<Match> all() {
        List<Match> l = new ArrayList<>(matches.values());
        l.sort(Comparator.comparingInt(x -> x.no));
        return l;
    }

    public Match get(String id) { return matches.get(id); }

    public Map<String, Object> summary(Match m) {
        List<Player> all = new ArrayList<>(m.teamA.players);
        all.addAll(m.teamB.players);
        Player top = all.stream().max(Comparator.comparingInt(p -> p.runs)).orElse(null);
        Player best = all.stream().filter(p -> p.ballsBowled > 0)
                .max(Comparator.<Player>comparingInt(p -> p.wickets).thenComparingInt(p -> -p.runsConceded)).orElse(null);
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("match", m.title);
        s.put("status", m.status);
        s.put("result", m.result);
        s.put(m.teamA.name, m.teamA.runs + "/" + m.teamA.wickets + " (" + m.teamA.getOvers() + ")");
        s.put(m.teamB.name, m.teamB.runs + "/" + m.teamB.wickets + " (" + m.teamB.getOvers() + ")");
        s.put("topScorer", top);
        s.put("bestBowler", best);
        return s;
    }

    public SseEmitter subscribe() {
        SseEmitter e = new SseEmitter(0L);
        emitters.add(e);
        e.onCompletion(() -> emitters.remove(e));
        e.onTimeout(() -> emitters.remove(e));
        e.onError(x -> emitters.remove(e));
        send(e);
        return e;
    }

    @Scheduled(fixedDelay = 1500)
    public void tick() {
        boolean live = false;
        for (Match m : matches.values()) {
            if ("LIVE".equals(m.status)) { bowl(m); live = true; }
        }
        if (!live) {
            int i = rnd.nextInt(TEAMS.length), j;
            do { j = rnd.nextInt(TEAMS.length); } while (j == i);
            createMatch(TEAMS[i], TEAMS[j], 10);
        }
        if (matches.size() > 12) { // keep memory bounded
            all().stream().filter(x -> "COMPLETED".equals(x.status)).limit(matches.size() - 12)
                    .forEach(x -> matches.remove(x.id));
        }
        emitters.forEach(this::send);
    }

    private void send(SseEmitter e) {
        try {
            e.send(SseEmitter.event().name("scores").data(all(), MediaType.APPLICATION_JSON));
        } catch (Exception ex) {
            emitters.remove(e);
        }
    }

    private void bowl(Match m) {
        synchronized (m) {
            if (!"LIVE".equals(m.status)) return;
            Team bat = m.batting(), bowl = m.bowling();
            Player s = bat.players.get(m.striker), b = bowl.players.get(m.bowler);
            s.batted = true;
            bat.players.get(m.nonStriker).batted = true;
            s.balls++; b.ballsBowled++; bat.balls++;
            int r = rnd.nextInt(100);
            String text;
            if (r >= 86) {
                s.out = true; b.wickets++; bat.wickets++;
                text = "WICKET! " + s.name + " b " + b.name + " " + s.runs + "(" + s.balls + ")";
                if (bat.wickets < 10) m.striker = m.next++;
            } else {
                int runs = r < 28 ? 0 : r < 52 ? 1 : r < 62 ? 2 : r < 64 ? 3 : r < 78 ? 4 : 6;
                s.runs += runs; b.runsConceded += runs; bat.runs += runs;
                if (runs == 4) s.fours++;
                if (runs == 6) s.sixes++;
                text = runs == 0 ? "dot ball" : runs == 4 ? "FOUR!" : runs == 6 ? "SIX!" : runs + " run" + (runs > 1 ? "s" : "");
                if (runs % 2 == 1) { int t = m.striker; m.striker = m.nonStriker; m.nonStriker = t; }
            }
            m.log(bat.getOvers() + "  " + b.name + " to " + s.name + ": " + text);
            if (bat.balls % 6 == 0) {
                int t = m.striker; m.striker = m.nonStriker; m.nonStriker = t;
                m.bowler = 6 + ((m.bowler - 6 + 1) % 5);
            }
            boolean chased = m.innings == 2 && bat.runs >= m.target;
            if (chased || bat.wickets >= 10 || bat.balls >= m.maxOvers * 6) endInnings(m);
        }
    }

    private void endInnings(Match m) {
        if (m.innings == 1) {
            m.target = m.teamA.runs + 1;
            m.innings = 2;
            m.striker = 0; m.nonStriker = 1; m.next = 2; m.bowler = 6;
            m.log("Innings break. " + m.teamB.name + " need " + m.target + " to win");
        } else {
            m.status = "COMPLETED";
            if (m.teamB.runs >= m.target) m.result = m.teamB.name + " won by " + (10 - m.teamB.wickets) + " wickets";
            else if (m.teamB.runs == m.target - 1) m.result = "Match tied";
            else m.result = m.teamA.name + " won by " + (m.target - 1 - m.teamB.runs) + " runs";
            m.log("RESULT: " + m.result);
        }
    }
}
