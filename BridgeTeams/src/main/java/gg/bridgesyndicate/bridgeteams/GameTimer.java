package gg.bridgesyndicate.bridgeteams;

public class GameTimer {

    private static GameTimer single_instance = null;
    private static int gameLengthInSeconds = 0;
    private long gameStartedUnixTime = 0;


    GameTimer(int gameLengthInSeconds) {
        this.gameLengthInSeconds = gameLengthInSeconds;
        gameStartedUnixTime = System.currentTimeMillis();
    }

    public long getGameStartedUnixTime() {
        return gameStartedUnixTime;
    }

    public static GameTimer getInstance() {
        if (single_instance == null)
            single_instance = new GameTimer(gameLengthInSeconds);
        return single_instance;
    }

    private long getRemainingTimeInMillis() {
        long gameLengthInMillis = gameLengthInSeconds * 1_000;
        final long endTime = gameStartedUnixTime + gameLengthInMillis;
        long currentTime = System.currentTimeMillis();
        return (endTime - currentTime);
    }

    public long getRemainingTimeInSeconds() {
        return (long) Math.floor(getRemainingTimeInMillis() / 1000);
    }

    public String formatSeconds(int seconds) {
        int minutes = seconds % 3600 / 60;
        return (String.format("%02d", minutes)
                + ":" + String.format("%02d", seconds % 60));
    }

    public String getRemainingTimeFormatted() {
        long remainingTimeInMillis = getRemainingTimeInMillis();
        int remainingTimeInSeconds = (int) Math.ceil((float) remainingTimeInMillis / 1000);
        return formatSeconds(remainingTimeInSeconds);
    }

    public int convertMillisToSeconds(long m) {
        return (int) Math.ceil((float) m / 1000);
    }

}
