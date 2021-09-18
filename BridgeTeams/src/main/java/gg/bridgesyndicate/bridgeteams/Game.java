package gg.bridgesyndicate.bridgeteams;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@JsonIgnoreProperties(value = { "most_recent_scorer_name" })
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Game {
    private int requiredPlayers=0;
    private final String dequeuedAt;
    private final String uuid = null;
    private int goalsToWin = 0;
    private int gameLengthInSeconds = 0;
    private final GameScore gameScore;
    public long gameStartedAt = 0;
    public long gameEndedAt = 0;
    public enum GameState { BEFORE_GAME, DURING_GAME, AFTER_GAME, CAGED, TERMINATE, ABORTED }
    private GameState state;
    private String taskArn;
    private List redTeamMinecraftUuids;
    private List blueTeamMinecraftUuids;
    private List redTeamDiscordIds;
    private List blueTeamDiscordIds;
    private List redTeamDiscordNames;
    private List blueTeamDiscordNames;
    private String queuedAt;
    private String queuedVia;
    private List acceptedByDiscordIds;
    private HashSet<String> joinedPlayers = new HashSet();
    private GameTimer gameTimer;
    private List<GoalMeta> goalsScored = new ArrayList<>();
    private List<KillMeta> killsRegistered = new ArrayList<>();
    private HashMap<String, UUID> playerMap = new HashMap();

    public Game() {
        this.state = state.BEFORE_GAME;
        this.dequeuedAt = Game.getIso8601NowString();
        this.gameScore = GameScore.getInstance();
    }

    /* METHODS */
    public String getDequeuedAt() { return dequeuedAt; }
    public String getQueuedAt() { return queuedAt; }
    public String getQueuedVia() { return queuedVia; }


    public String getUuid() {
        return uuid;
    }

    public int getGameLengthInSeconds() {
        return gameLengthInSeconds;
    }

    public int getGoalsToWin() {
        return goalsToWin;
    }

    public String getTaskArn() {
        return taskArn;
    }

    public HashMap getGameScore() {
        HashMap<String, Integer> score = new HashMap<>();
        score.put("red", GameScore.getRed());
        score.put("blue", GameScore.getBlue());
        return(score);
    }

    public String getFinalGameLengthFormatted() {
        if (gameTimer == null) return("");
        int finalGameLengthInSeconds = gameTimer.convertMillisToSeconds(gameEndedAt - gameStartedAt);
        return gameTimer.formatSeconds(finalGameLengthInSeconds);
    }

    public List<GoalMeta> getGoalsScored() { return goalsScored; }
    public List<KillMeta> getKillsRegistered() { return killsRegistered; }

    public void setEndTime() {
        gameEndedAt = System.currentTimeMillis();
    }

    public void addContainerMetaData() throws URISyntaxException, IOException {
        String url = System.getenv("ECS_CONTAINER_METADATA_URI_V4");
        ContainerMetadata containerMetaData = new ContainerMetadata(url);
        this.taskArn = containerMetaData.getTaskArn();
    }

    public Iterator<String> getJoinedPlayers() {
        return joinedPlayers.iterator();
    }

    public List getRedTeamMinecraftUuids() {
        return redTeamMinecraftUuids;
    }

    public List getBlueTeamMinecraftUuids() {
        return blueTeamMinecraftUuids;
    }

    public List getRedTeamDiscordIds() {
        return redTeamDiscordIds;
    }

    public List getBlueTeamDiscordIds() {
        return blueTeamDiscordIds;
    }

    public List getRedTeamDiscordNames() {
        return redTeamDiscordNames;
    }

    public List getBlueTeamDiscordNames() {
        return blueTeamDiscordNames;
    }
    public List getAcceptedByDiscordIds(){
        return acceptedByDiscordIds;
    }
    public String getRemainingTimeFormatted() {
        if (gameTimer == null) return("");
        return gameTimer.getRemainingTimeFormatted();
    }

    public long getRemainingTimeInSeconds() {
        if (gameTimer == null) return(-1);
        return gameTimer.getRemainingTimeInSeconds();
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
        return(player.getName());
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

    static String serialize(Game game) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return( mapper.writerWithDefaultPrettyPrinter().writeValueAsString(game) );
    }

    static Game deserialize(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        Game game = null;
        try {
            game = objectMapper.readValue(json, Game.class);
        } catch (JsonProcessingException e) {
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
            gameTimer = new GameTimer(gameLengthInSeconds);
            gameStartedAt = gameTimer.getGameStartedUnixTime();
        }
        this.state = state;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public static void main(String[] args) throws IOException {
        double futureHealth = 17.19234324;
        double formattedFutureHealth = Math.ceil(futureHealth * 10)/10;
        System.out.println(formattedFutureHealth);
//        String url = System.getenv("ECS_CONTAINER_METADATA_URI_V4");
//        ContainerMetadata containerMetaData = new ContainerMetadata(url);
//        ObjectMapper mapper = new ObjectMapper();
//        System.out.println( mapper.writeValueAsString(containerMetaData) );
    }

    public boolean hasPlayer(Player player) {
        return (getRedTeamMinecraftUuids().contains(player.getUniqueId().toString()) ||
                getBlueTeamMinecraftUuids().contains(player.getUniqueId().toString()));
    }

    public TeamType getTeam(Player player) {
        if (getRedTeamMinecraftUuids().contains(player.getUniqueId().toString())) {
            return (TeamType.RED);
        } else {
            return (TeamType.BLUE);
        }
    }

    public void playerJoined(String name) {
        Player player = Bukkit.getPlayer(name);
        joinedPlayers.add(name);
        playerMap.put(name, player.getUniqueId());
    }

    public HashMap<String, UUID> getPlayerMap(){
        return(playerMap);
    }

    public boolean over() {
        return(GameScore.getBlue() == goalsToWin || GameScore.getRed() == goalsToWin);
    }

    public static String getIso8601NowString() {
        return(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    class GoalMeta {
        private final UUID playerUUID;
        private final long goalTime;

        GoalMeta(UUID playerUUID, long goalTime){
            this.playerUUID = playerUUID;
            this.goalTime = goalTime;
        }

        public long getGoalTime() { return goalTime; }
        public UUID getPlayerUUID() {
            return playerUUID;
        }
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
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
        public long getKillTime() { return killTime; }
    }


}
