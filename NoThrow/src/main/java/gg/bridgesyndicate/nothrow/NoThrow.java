package gg.bridgesyndicate.nothrow;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class NoThrow extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        System.out.println(this.getClass() + " is loading.");
        getServer().getPluginManager().registerEvents(this, this);
    }


    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player p = event.getPlayer();

        if (event.getItemDrop().getItemStack().getType() == Material.BOW || event.getItemDrop().getItemStack().getType() == Material.ARROW || event.getItemDrop().getItemStack().getType() == Material.GOLDEN_APPLE || event.getItemDrop().getItemStack().getType() == Material.IRON_SWORD || event.getItemDrop().getItemStack().getType() == Material.DIAMOND_PICKAXE || event.getItemDrop().getItemStack().getType() == Material.STAINED_CLAY) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent e) {
        Material clickedItem = e.getClickedInventory().getItem(e.getSlot()).getType();

        if (clickedItem == Material.ARROW) {
            e.setCancelled(true);
        }

        if( e.getSlotType() == InventoryType.SlotType.ARMOR) {
            e.setCancelled(true);
        }
    }


}
