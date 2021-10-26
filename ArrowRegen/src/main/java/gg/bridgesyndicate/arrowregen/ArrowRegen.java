package gg.bridgesyndicate.arrowregen;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ArrowRegen extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        System.out.println(this.getClass() + " is loading.");
        System.out.println(this.getClass() + " is now no longer needed; methods moved to main plugin (BridgeTeams)");

        // We should start removing this from the build scripts etc. We should do the same with NoThrow BridgeGaps as well
    }
}
