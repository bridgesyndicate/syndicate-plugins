package gg.bridgesyndicate.bridgeteams;

public class GameTimer {

    private static GameTimer single_instance = null;
    private long gameStartedUnixTime = 0;

    GameTimer() {
        gameStartedUnixTime = System.currentTimeMillis();
    }

    public long getGameStartedUnixTime() {
        return gameStartedUnixTime;
    }

    public static GameTimer getInstance() {
        if (single_instance == null)
            single_instance = new GameTimer();
        return single_instance;
    }

    private long getRemainingTimeInMillis() {
        long GAME_LENGTH_IN_MILLIS = 300_000;
        final long endTime = gameStartedUnixTime + GAME_LENGTH_IN_MILLIS;
        long currentTime = System.currentTimeMillis();
        return(endTime - currentTime);
    }

    public long getRemainingTimeInSeconds(){
        return(getRemainingTimeInMillis()/1000);
    }

    public String getRemainingTimeFormatted(){
        long remainingTimeInMillis = getRemainingTimeInMillis();
        int remainingTimeInSeconds = (int) Math.ceil( (float) remainingTimeInMillis / 1000 );
        int remainingMinutes = remainingTimeInSeconds % 3600 / 60;
        return(String.format("%02d", remainingMinutes)
                + ":" + String.format("%02d", remainingTimeInSeconds % 60));
    }
}
