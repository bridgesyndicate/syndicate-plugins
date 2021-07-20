package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

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
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateBlueGoals(blue, player);
            }
        } else {
            red++;
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateRedGoals(red, player);
            }
        }
    }

    public static void updateRedGoals(int red, Player player){
        Scoreboard board = player.getScoreboard();
        Objective title = board.getObjective("title");
        switch (red) {
            case 0: {
                Score redgoals = title.getScore(ChatColor.RED + "[R] " + ChatColor.GRAY + "⬤⬤⬤⬤⬤");
                redgoals.setScore(10);
                break;
            }
            case 1: {
                Score redgoals = title.getScore(ChatColor.RED + "[R] " + ChatColor.GRAY + "⬤⬤⬤⬤⬤");
                redgoals.setScore(0);
                Score redgoals1 = title.getScore(ChatColor.RED + "[R] " + ChatColor.RED + "⬤" + ChatColor.GRAY + "⬤⬤⬤⬤");
                redgoals1.setScore(10);
                break;
            }
            case 2: {
                Score redgoals1 = title.getScore(ChatColor.RED + "[R] " + ChatColor.RED + "⬤" + ChatColor.GRAY + "⬤⬤⬤⬤");
                redgoals1.setScore(0);
                Score redgoals2 = title.getScore(ChatColor.RED + "[R] " + ChatColor.RED + "⬤⬤" + ChatColor.GRAY + "⬤⬤⬤");
                redgoals2.setScore(10);
                break;
            }
            case 3: {
                Score redgoals2 = title.getScore(ChatColor.RED + "[R] " + ChatColor.RED + "⬤⬤" + ChatColor.GRAY + "⬤⬤⬤");
                redgoals2.setScore(0);
                Score redgoals3 = title.getScore(ChatColor.RED + "[R] " + ChatColor.RED + "⬤⬤⬤" + ChatColor.GRAY + "⬤⬤");
                redgoals3.setScore(10);
                break;
            }
            case 4: {
                Score redgoals3 = title.getScore(ChatColor.RED + "[R] " + ChatColor.RED + "⬤⬤⬤" + ChatColor.GRAY + "⬤⬤");
                redgoals3.setScore(0);
                Score redgoals4 = title.getScore(ChatColor.RED + "[R] " + ChatColor.RED + "⬤⬤⬤⬤" + ChatColor.GRAY + "⬤");
                redgoals4.setScore(10);
                break;
            }
            case 5: {
                Score redgoals4 = title.getScore(ChatColor.RED + "[R] " + ChatColor.RED + "⬤⬤⬤⬤" + ChatColor.GRAY + "⬤");
                redgoals4.setScore(0);
                Score redgoals5 = title.getScore(ChatColor.RED + "[R] " + ChatColor.RED + "⬤⬤⬤⬤⬤" + ChatColor.GRAY + "");
                redgoals5.setScore(10);
                break;
            }
        }
    }

    public static void updateBlueGoals(int blue, Player player){
        Scoreboard board = player.getScoreboard();
        Objective title = board.getObjective("title");
        switch (blue) {
            case 0: {
                Score bluegoals = title.getScore(ChatColor.BLUE + "[B] " + ChatColor.GRAY + "⬤⬤⬤⬤⬤");
                bluegoals.setScore(10);
                break;
            }
            case 1: {
                Score bluegoals = title.getScore(ChatColor.BLUE + "[B] " + ChatColor.GRAY + "⬤⬤⬤⬤⬤");
                bluegoals.setScore(0);
                Score bluegoals1 = title.getScore(ChatColor.BLUE + "[B] " + ChatColor.BLUE + "⬤" + ChatColor.GRAY + "⬤⬤⬤⬤");
                bluegoals1.setScore(10);
                break;
            }
            case 2: {
                Score bluegoals1 = title.getScore(ChatColor.BLUE + "[B] " + ChatColor.BLUE + "⬤" + ChatColor.GRAY + "⬤⬤⬤⬤");
                bluegoals1.setScore(0);
                Score bluegoals2 = title.getScore(ChatColor.BLUE + "[B] " + ChatColor.BLUE + "⬤⬤" + ChatColor.GRAY + "⬤⬤⬤");
                bluegoals2.setScore(10);
                break;
            }
            case 3: {
                Score bluegoals2 = title.getScore(ChatColor.BLUE + "[B] " + ChatColor.BLUE + "⬤⬤" + ChatColor.GRAY + "⬤⬤⬤");
                bluegoals2.setScore(0);
                Score bluegoals3 = title.getScore(ChatColor.BLUE + "[B] " + ChatColor.BLUE + "⬤⬤⬤" + ChatColor.GRAY + "⬤⬤");
                bluegoals3.setScore(10);
                break;
            }
            case 4: {
                Score bluegoals3 = title.getScore(ChatColor.BLUE + "[B] " + ChatColor.BLUE + "⬤⬤⬤" + ChatColor.GRAY + "⬤⬤");
                bluegoals3.setScore(0);
                Score bluegoals4 = title.getScore(ChatColor.BLUE + "[B] " + ChatColor.BLUE + "⬤⬤⬤⬤" + ChatColor.GRAY + "⬤");
                bluegoals4.setScore(10);
                break;
            }
            case 5: {
                Score bluegoals4 = title.getScore(ChatColor.BLUE + "[B] " + ChatColor.BLUE + "⬤⬤⬤⬤" + ChatColor.GRAY + "⬤");
                bluegoals4.setScore(0);
                Score bluegoals5 = title.getScore(ChatColor.BLUE + "[B] " + ChatColor.BLUE + "⬤⬤⬤⬤⬤" + ChatColor.GRAY + "");
                bluegoals5.setScore(10);
                break;
            }
        }
    }

    public void printScore() {
        Bukkit.broadcastMessage("The score is red:" + this.getRed() + ", blue:" + this.getBlue());
    }
}


