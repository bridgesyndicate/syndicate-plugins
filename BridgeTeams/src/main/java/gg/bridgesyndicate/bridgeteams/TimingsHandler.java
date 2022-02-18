package gg.bridgesyndicate.bridgeteams;
import org.bukkit.Bukkit;

public class TimingsHandler {

    public static void turnTimingsOn() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "timings on");
    }

    public static void pasteTimings() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "timings paste");
    }

}
