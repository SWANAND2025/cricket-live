package com.example.cricket;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MatchController {
    private final MatchService svc;

    public MatchController(MatchService svc) { this.svc = svc; }

    @GetMapping("/matches")
    public List<Match> all() { return svc.all(); }

    @GetMapping("/matches/{id}")
    public ResponseEntity<Match> one(@PathVariable("id") String id) {
        Match m = svc.get(id);
        return m == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(m);
    }

    @GetMapping("/matches/{id}/players")
    public ResponseEntity<Map<String, List<Player>>> players(@PathVariable("id") String id) {
        Match m = svc.get(id);
        if (m == null) return ResponseEntity.notFound().build();
        Map<String, List<Player>> r = new LinkedHashMap<>();
        r.put(m.teamA.name, m.teamA.players);
        r.put(m.teamB.name, m.teamB.players);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/matches/{id}/summary")
    public ResponseEntity<Map<String, Object>> summary(@PathVariable("id") String id) {
        Match m = svc.get(id);
        return m == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(svc.summary(m));
    }

    @PostMapping("/matches")
    public Match create(@RequestParam("teamA") String a, @RequestParam("teamB") String b,
                        @RequestParam(value = "overs", defaultValue = "10") int overs) {
        return svc.createMatch(a, b, overs);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() { return svc.subscribe(); }
}
