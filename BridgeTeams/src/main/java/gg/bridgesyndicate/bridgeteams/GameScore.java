package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Iterator;

class GameScore { // Singleton
    private static GameScore single_instance = null;
    public static int red;
    public static int blue;
    public enum scoreboardSections { RED_SCORE, BLUE_SCORE, KILLS, GOALS }
    private static final String BUBBLE = "⬤";
    private static final String RED_SCORE_LINE = ChatColor.RED + "[R] " + ChatColor.GRAY;
    private static final String BLUE_SCORE_LINE = ChatColor.BLUE + "[B] " + ChatColor.GRAY;
    private static final String KILLS_LINE = ChatColor.WHITE + "Kills: ";
    private static final String GOALS_LINE = ChatColor.WHITE + "Goals: ";
    private static int goalsToWin = 0;

    private GameScore() {
        red = 0;
        blue = 0;
    }

    /* METHODS */

    public static GameScore getInstance() {
        if (single_instance == null)
            single_instance = new GameScore();
        return single_instance;
    }

    public static int getRed() {
        return red;
    }

    public static int getBlue() {
        return blue;
    }

    public static void initialize(Scoreboard board, Objective objective, Game game) {
        goalsToWin = game.goalsToWin;
        Team redScore = board.registerNewTeam(String.valueOf(scoreboardSections.RED_SCORE));
        redScore.addEntry(RED_SCORE_LINE);
        redScore.setSuffix(new String(new char[goalsToWin]).replace("\0", BUBBLE));
        redScore.setPrefix("");
        objective.getScore(RED_SCORE_LINE).setScore(0);

        Team blueScore = board.registerNewTeam(String.valueOf(scoreboardSections.BLUE_SCORE));
        blueScore.addEntry(BLUE_SCORE_LINE);
        blueScore.setSuffix(new String(new char[goalsToWin]).replace("\0", BUBBLE));
        blueScore.setPrefix("");
        objective.getScore(BLUE_SCORE_LINE).setScore(0);

        Team kills = board.registerNewTeam(String.valueOf(scoreboardSections.KILLS));
        kills.addEntry(KILLS_LINE);
        kills.setSuffix(ChatColor.GREEN + "0");
        kills.setPrefix("");
        objective.getScore(KILLS_LINE).setScore(0);

        Team goals = board.registerNewTeam(String.valueOf(scoreboardSections.GOALS));
        goals.addEntry(GOALS_LINE);
        goals.setSuffix(ChatColor.GREEN + "0");
        goals.setPrefix("");
        objective.getScore(GOALS_LINE).setScore(0);
    }

    public void increment(TeamType playerTeam) {
        if (playerTeam == TeamType.BLUE) {
            blue++;
        } else {
            red++;
        }
        updateScoreboardScore();
    }

    private void updateScoreboardScore() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();
            updateScoreBubbles(board);
        }
    }

    private void updateScoreBubbles(Scoreboard board) {
        Team redScore = board.getTeam(String.valueOf(scoreboardSections.RED_SCORE));
        redScore.setSuffix(getBubbles(red, ChatColor.RED));
        Team blueScore = board.getTeam(String.valueOf(scoreboardSections.BLUE_SCORE));
        blueScore.setSuffix(getBubbles(blue, ChatColor.BLUE));
    }

    public void updateKillersKills(Player killer, Game game) {
        Scoreboard board = killer.getScoreboard();
        Team kills = board.getTeam(String.valueOf(scoreboardSections.KILLS));
        kills.setSuffix(ChatColor.GREEN + "" + game.getNumberOfKillsForPlayer(killer) );
    }

    public void updatePlayersGoals(Player player, Game game) {
        Scoreboard board = player.getScoreboard();
        Team goals = board.getTeam(String.valueOf(scoreboardSections.GOALS));
        goals.setSuffix(ChatColor.GREEN + "" + game.getNumberOfGoalsForPlayer(player) );
    }

    private String getBubbles(int coloredBubbles, ChatColor teamColor) {
        String bubbles = "";
        int idx = 0;
        while (idx < goalsToWin) {
            if (coloredBubbles > idx) { // team-colored bubble
                bubbles = bubbles.concat(teamColor + BUBBLE);
            } else { //grey bubble
                bubbles = bubbles.concat(ChatColor.GRAY + BUBBLE);
            }
            idx++;
        }
        return(bubbles);
    }

    public void printScore() {
        Bukkit.broadcastMessage("The score is red:" + this.getRed() + ", blue:" + this.getBlue());
    }

    public void updateGameClock(Game game) {
        String timeRemaining = game.getRemainingTimeFormatted();
        for (Iterator<String> it = game.getJoinedPlayers(); it.hasNext(); ) {
            String playerName = it.next();
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                Scoreboard board = player.getScoreboard();
                Team timer = board.getTeam(String.valueOf(BridgeTeams.scoreboardSections.TIMER));
                timer.setSuffix(timeRemaining);
            }
        }
    }
}


