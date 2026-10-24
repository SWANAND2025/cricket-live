package com.example.cricket;

import java.util.ArrayList;
import java.util.List;

public class Team {
    public String name;
    public List<Player> players = new ArrayList<>();
    public int runs, wickets, balls;

    public Team(String name) {
        this.name = name;
        for (int i = 1; i <= 11; i++) players.add(new Player(name + " Player " + i));
    }

    public String getOvers() { return balls / 6 + "." + balls % 6; }
}
