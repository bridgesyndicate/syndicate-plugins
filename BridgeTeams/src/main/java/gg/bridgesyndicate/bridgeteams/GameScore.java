package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;

class GameScore {
    private static GameScore single_instance = null;

    public int red;
    public int blue;

    private GameScore() {
        red = 0;
        blue = 0;
    }

    public static GameScore getInstance() {
        if (single_instance == null)
            single_instance = new GameScore();
        return single_instance;
    }

    public int getRed() {
        return red;
    }

    public int getBlue() {
        return blue;
    }

    public void increment(TeamType playerTeam) {
        if (playerTeam == TeamType.BLUE) {
            blue++;
        } else {
            red++;
        }
    }

    public void printScore() {
        Bukkit.broadcastMessage("The score is red:" + this.getRed() + ", blue:" + this.getBlue());
    }
}


