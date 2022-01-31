package gg.bridgesyndicate.util;

public class Seconds {

    public static int toTicks(float seconds){
        int ticks = (int) (seconds * 20);
        return ticks;
    }

}
