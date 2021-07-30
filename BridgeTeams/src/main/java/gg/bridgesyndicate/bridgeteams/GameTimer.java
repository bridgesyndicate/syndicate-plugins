package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.concurrent.TimeUnit;

public class GameTimer {

    private static GameTimer single_instance = null;
    private long gameStartedUnixTime;
    private final long GAME_LENGTH_IN_MILLIS = 900_000;

    GameTimer() {
        gameStartedUnixTime = System.currentTimeMillis();
    }

    public static GameTimer getInstance() {
        if (single_instance == null)
            single_instance = new GameTimer();
        return single_instance;
    }

    public String getRemainingTime(){
        long endTime = gameStartedUnixTime + GAME_LENGTH_IN_MILLIS;
        long currentTime = System.currentTimeMillis();
        long remainingTimeInMillis = endTime - currentTime;
        int remainingTimeInSeconds = (int) Math.ceil( (float) remainingTimeInMillis / 1000 );
        int remainingMinutes = remainingTimeInSeconds % 3600 / 60;
        return(
                String.format("%02d", remainingMinutes) +
                        ":" +
                        String.format("%02d", remainingTimeInSeconds % 60));
    }
}




