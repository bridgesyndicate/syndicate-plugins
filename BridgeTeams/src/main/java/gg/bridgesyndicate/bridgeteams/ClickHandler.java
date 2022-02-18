//package gg.bridgesyndicate.bridgeteams;
//
//
//import net.minecraft.server.v1_8_R3.MinecraftServer;
//import org.bukkit.ChatColor;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.block.BlockPlaceEvent;
//
//import java.util.HashMap;
//import java.util.UUID;
//
//public class ClickHandler implements Listener {
//
//    public ClickHandler(){}
//
//    public static HashMap<UUID, Integer> lastBlockPlaceTimestamp = new HashMap<>();
//    public static HashMap<UUID, Integer> lastPlaced2BlocksIn2Ticks = new HashMap<>();
//
//    @EventHandler
//    public void onBlockPlace(BlockPlaceEvent event) {
//
//        Player player = event.getPlayer();
//        UUID id = player.getUniqueId();
//        if(lastPlaced2BlocksIn2Ticks.get(id) != null) {
//            if(MinecraftServer.currentTick - lastPlaced2BlocksIn2Ticks.get(id) < 2){
//                player.sendMessage(ChatColor.DARK_RED + "Prevented right-click mouse abuse.");
//                event.setCancelled(true);
//                // 3) if player places 3 blocks in 3 ticks, cancel
//            }
//            lastPlaced2BlocksIn2Ticks.remove(id);
//        }
//        if(lastBlockPlaceTimestamp.get(id) != null) {
//            if ((MinecraftServer.currentTick - lastBlockPlaceTimestamp.get(id)) < 2) {
//                lastPlaced2BlocksIn2Ticks.put(id, MinecraftServer.currentTick);
//                // 2) mark down when a player places 2 blocks in 2 ticks
//            }
//        }
//        lastBlockPlaceTimestamp.put(id, MinecraftServer.currentTick);
//        // 1) plot timestamp of when player places a block
//    }
//
//}
