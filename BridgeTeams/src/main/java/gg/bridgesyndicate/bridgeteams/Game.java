package gg.bridgesyndicate.bridgeteams;


import org.apache.juneau.annotation.Beanc;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.parser.ParseException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class Game {
    private final int requiredPlayers;

    public enum GameState { BEFORE_GAME, DURING_GAME, AFTER_GAME, CAGED }
    private GameState state;
    private ContainerMetadata containerMetadata;
    private List redTeam;
    private List blueTeam;
    private HashSet<String> joinedPlayers = new HashSet();
    private GameTimer gameTimer;
    private List<GoalMeta> goalsScored = new ArrayList<>();
    private List<KillMeta> killsRegistered = new ArrayList<>();


    @Beanc(properties = "requiredPlayers,blueTeam,redTeam")
    public Game(int requiredPlayers, List blueTeam, List redTeam) {
        this.requiredPlayers = requiredPlayers;
        this.blueTeam = blueTeam;
        this.redTeam = redTeam;
        this.state = state.BEFORE_GAME;
    }

    /* METHODS */

    public void addContainerMetaData() throws URISyntaxException, IOException {
        containerMetadata = new ContainerMetadata();
    }

    public Iterator<String> getJoinedPlayers() {
        return joinedPlayers.iterator();
    }

    public List getRedTeam() {
        return redTeam;
    }

    public List getBlueTeam() {
        return blueTeam;
    }

    public String getRemainingTime() {
        return gameTimer.getRemainingTime();
    }

    public void addGoalInfo(UUID scorerId) {
        GoalMeta newGoal = new GoalMeta(scorerId, System.currentTimeMillis());
        goalsScored.add(newGoal);
    }

    public void addKillInfo(UUID killer) {
        KillMeta newKill = new KillMeta(killer, System.currentTimeMillis());
        killsRegistered.add(newKill);
    }

    public String getMostRecentScorerName() {
        GoalMeta lastGoal = goalsScored.get(goalsScored.size() - 1);
        Player player = Bukkit.getPlayer(lastGoal.getPlayerUUID());
        ChatColor chatColor = MatchTeam.getChatColor(player);
        return(chatColor + player.getName());
    }

    public boolean hasScore() {
        return (goalsScored.size() > 0);
    }

    public int getNumberOfGoalsForPlayer(Player player) {
        int totalGoals = 0;
        for (Iterator<GoalMeta> it = goalsScored.iterator(); it.hasNext(); ) {
            GoalMeta goalMeta = it.next();
            if (goalMeta.getPlayerUUID() == player.getUniqueId()){
                totalGoals++;
            }
        }
        return(totalGoals);
    }

    public int getNumberOfKillsForPlayer(Player player) {
        int totalKills = 0;
        for (Iterator<KillMeta> it = killsRegistered.iterator(); it.hasNext(); ) {
            KillMeta killMeta = it.next();
            if (killMeta.getPlayerUUID() == player.getUniqueId()){
                totalKills++;
            }
        }
        return(totalKills);
    }

    static Game juneauGameFactory(String json) {
        JsonParser jsonParser = JsonParser.DEFAULT;
        Game game = null;
        try {
            game = jsonParser.parse(json, Game.class);
        } catch (ParseException e) {
            System.err.println("Cannot parse game json.");
            e.printStackTrace();
        }
        return(game);
    }

    public int getNumberOfJoinedPlayers() {
        return joinedPlayers.size();
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        if (this.state == GameState.BEFORE_GAME && state == GameState.CAGED) { //only happens once
            gameTimer = new GameTimer();
        }
        this.state = state;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public static void main(String[] args) throws InterruptedException {
        long gameStartedUnixTime = System.currentTimeMillis();
        long endTime = gameStartedUnixTime + 900_000;
        while (true) {
            long currentTime = System.currentTimeMillis();
            long remainingTimeInMillis = endTime - currentTime;
            int remainingTimeInSeconds = (int) Math.ceil( (float) remainingTimeInMillis / 1000 );
            int remainingMinutes = remainingTimeInSeconds % 3600 / 60;
            System.out.println(
                        String.format("%02d", remainingMinutes) +
                                ":" +
                                String.format("%02d", remainingTimeInSeconds % 60));
            java.lang.Thread.sleep(1000);
        }
    }

    public boolean hasPlayer(Player player) {
        return (getRedTeam().contains(player.getName()) ||
                getBlueTeam().contains(player.getName()));
    }

    public TeamType getTeam(Player player) {
        if (getRedTeam().contains(player.getName())) {
            return (TeamType.RED);
        } else {
            return (TeamType.BLUE);
        }
    }

    public void playerJoined(String name) {
        joinedPlayers.add(name);
    }

    class GoalMeta {
        private final UUID playerUUID;
        private final long goalTime;

        GoalMeta(UUID playerUUID, long goalTime){
            this.playerUUID = playerUUID;
            this.goalTime = goalTime;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

    }

    class KillMeta {
        private final UUID playerUUID;
        private final long killTime;

        KillMeta(UUID playerUUID, long goalTime){
            this.playerUUID = playerUUID;
            this.killTime = goalTime;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

    }

}
