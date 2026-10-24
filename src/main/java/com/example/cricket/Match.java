package com.example.cricket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Match {
    public int no;
    public String id, title, status = "LIVE", result = "";
    public int maxOvers, innings = 1, target;
    public Team teamA, teamB; // teamA bats first
    public List<String> commentary = new CopyOnWriteArrayList<>();

    @JsonIgnore public int striker = 0, nonStriker = 1, next = 2, bowler = 6;

    @JsonIgnore public Team batting() { return innings == 1 ? teamA : teamB; }
    @JsonIgnore public Team bowling() { return innings == 1 ? teamB : teamA; }

    public String getBattingTeam() { return batting().name; }
    public String getStrikerName() { return batting().players.get(striker).name; }
    public String getNonStrikerName() { return batting().players.get(nonStriker).name; }
    public String getBowlerName() { return bowling().players.get(bowler).name; }

    public void log(String s) {
        commentary.add(0, s);
        while (commentary.size() > 40) commentary.remove(commentary.size() - 1);
    }
}
