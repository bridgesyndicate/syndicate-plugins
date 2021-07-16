package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;

class Score {
    private static Score single_instance = null;

    public int red;
    public int blue;

    private Score() {
        red = 0;
        blue = 0;
    }

    public static Score getInstance() {
        if (single_instance == null)
            single_instance = new Score();
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


