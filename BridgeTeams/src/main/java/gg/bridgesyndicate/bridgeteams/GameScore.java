package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.Iterator;

class GameScore { // Singleton
    private static GameScore single_instance = null;
    public static int red;
    public static int blue;
    public enum scoreboardSections { RED_SCORE, BLUE_SCORE, KILLS, GOALS, IP, BLANK1, MODE }
    private static final String BUBBLE = "⬤";
    private static final String RED_SCORE_LINE = ChatColor.RED + "[R] " + ChatColor.GRAY;
    private static final String BLUE_SCORE_LINE = ChatColor.BLUE + "[B] " + ChatColor.GRAY;
    private static final String KILLS_LINE = ChatColor.WHITE + "Kills: ";
    private static final String GOALS_LINE = ChatColor.WHITE + "Goals: ";
    private static final String MODE_LINE = ChatColor.WHITE + "Mode: ";
    private static final String IP_LINE = ChatColor.YELLOW + "bridgesyndicate.gg";
    private static final String BLANK1_LINE = "";

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

    public static void initHealthTags(Scoreboard board, Player player) {
        Objective showhealth = board.registerNewObjective("showhealth", Criterias.HEALTH);
        showhealth.setDisplaySlot(DisplaySlot.BELOW_NAME);
        showhealth.setDisplayName(ChatColor.RED + "❤");
    }

    public static void initColorTags(Scoreboard board) {
        Team redTeamTag = board.registerNewTeam("RedTeamTag");
        Team blueTeamTag = board.registerNewTeam("BlueTeamTag");
        for (Player player : Bukkit.getOnlinePlayers()) {
            Team chosenTeam = null;
            ChatColor chosenColor = null;
            if (MatchTeam.getTeam(player) == TeamType.RED) {
                chosenColor = ChatColor.RED;
                chosenTeam = redTeamTag;
            } else {
                chosenColor = ChatColor.BLUE;
                chosenTeam = blueTeamTag;
            }
            chosenTeam.setPrefix(chosenColor.toString());
            chosenTeam.addPlayer(player);
        }
    }

    public static void initialize(Scoreboard board, Objective objective, Game game, Player player) {
        goalsToWin = game.getGoalsToWin();
        Team redScore = board.registerNewTeam(String.valueOf(scoreboardSections.RED_SCORE));
        redScore.addEntry(RED_SCORE_LINE);
        redScore.setSuffix(new String(new char[goalsToWin]).replace("\0", BUBBLE));
        redScore.setPrefix("");
        objective.getScore(RED_SCORE_LINE).setScore(6);

        Team blueScore = board.registerNewTeam(String.valueOf(scoreboardSections.BLUE_SCORE));
        blueScore.addEntry(BLUE_SCORE_LINE);
        blueScore.setSuffix(new String(new char[goalsToWin]).replace("\0", BUBBLE));
        blueScore.setPrefix("");
        objective.getScore(BLUE_SCORE_LINE).setScore(5);

        Team kills = board.registerNewTeam(String.valueOf(scoreboardSections.KILLS));
        kills.addEntry(KILLS_LINE);
        kills.setSuffix(ChatColor.GREEN + "0");
        kills.setPrefix("");
        objective.getScore(KILLS_LINE).setScore(4);

        Team goals = board.registerNewTeam(String.valueOf(scoreboardSections.GOALS));
        goals.addEntry(GOALS_LINE);
        goals.setSuffix(ChatColor.GREEN + "0");
        goals.setPrefix("");
        objective.getScore(GOALS_LINE).setScore(3);

        Team mode = board.registerNewTeam(String.valueOf(scoreboardSections.MODE));
        mode.addEntry(MODE_LINE);
        int playersPerTeam = game.getRequiredPlayers()/2;
        mode.setSuffix(ChatColor.GREEN + "Bridge " + playersPerTeam + "v" + playersPerTeam);
        mode.setPrefix("");
        objective.getScore(MODE_LINE).setScore(2);


        Team blank1 = board.registerNewTeam(String.valueOf(scoreboardSections.BLANK1));
        blank1.addEntry(BLANK1_LINE);
        blank1.setSuffix("");
        blank1.setPrefix("");
        objective.getScore(BLANK1_LINE).setScore(1);

        Team ip = board.registerNewTeam(String.valueOf(scoreboardSections.IP));
        ip.addEntry(IP_LINE);
        ip.setSuffix("");
        ip.setPrefix("");
        objective.getScore(IP_LINE).setScore(0);

        //initHealthTags(board, player);
        initColorTags(board);
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
        kills.setSuffix(ChatColor.GREEN + "" + game.getNumberOfKillsForPlayer(killer));
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


