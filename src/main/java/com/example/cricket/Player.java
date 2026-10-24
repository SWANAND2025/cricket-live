package com.example.cricket;

public class Player {
    public String name;
    public int runs, balls, fours, sixes, wickets, runsConceded, ballsBowled;
    public boolean out, batted;

    public Player(String name) { this.name = name; }

    public double getStrikeRate() { return balls == 0 ? 0 : Math.round(runs * 10000.0 / balls) / 100.0; }
    public String getBowlingOvers() { return ballsBowled / 6 + "." + ballsBowled % 6; }
    public double getEconomy() { return ballsBowled == 0 ? 0 : Math.round(runsConceded * 600.0 / ballsBowled) / 100.0; }
}
