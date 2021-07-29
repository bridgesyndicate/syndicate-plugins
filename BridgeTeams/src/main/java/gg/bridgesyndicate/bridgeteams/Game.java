package gg.bridgesyndicate.bridgeteams;


import org.apache.juneau.annotation.Beanc;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.parser.ParseException;
import org.apache.juneau.serializer.SerializeException;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Game {
    private final int requiredPlayers;
    private GameState state;
    private List redTeam;
    private List blueTeam;
    private HashSet<String> joinedPlayers = new HashSet();
    private GameTimer gameTimer;

    public Iterator<String> getJoinedPlayers() {
        return joinedPlayers.iterator();
    }

    public List getRedTeam() {
        return redTeam;
    }

    public List getBlueTeam() {
        return blueTeam;
    }

    public void setRedTeam(List redTeam) {
        this.redTeam = redTeam;
    }

    public void setBlueTeam(List blueTeam) {
        this.blueTeam = blueTeam;
    }

    public String getRemainingTime() {
        return gameTimer.getRemainingTime();
    }

    public enum GameState { BEFORE_GAME, DURING_GAME, AFTER_GAME, CAGED };

    @Beanc(properties = "requiredPlayers,blueTeam,redTeam")
    public Game(int requiredPlayers, List blueTeam, List redTeam) {
        this.requiredPlayers = requiredPlayers;
        this.blueTeam = blueTeam;
        this.redTeam = redTeam;
        this.state = state.BEFORE_GAME;
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

    public static void main(String[] args) throws SerializeException, ParseException, InterruptedException {
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
}
