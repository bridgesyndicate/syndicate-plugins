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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(InventoryClickEvent e){
        ItemStack i = e.getWhoClicked().getInventory().getItem(0);
        if(i != null)
        {
            if(e.getSlot() == 0 && i.getType() == Material.BOW || i.getType() == Material.ARROW || i.getType() == Material.GOLDEN_APPLE || i.getType() == Material.IRON_SWORD || i.getType() == Material.DIAMOND_PICKAXE || i.getType() == Material.STAINED_CLAY)
            {
                e.setCancelled(false);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player p = event.getPlayer();

        if (event.getItemDrop().getItemStack().getType() == Material.BOW || event.getItemDrop().getItemStack().getType() == Material.ARROW || event.getItemDrop().getItemStack().getType() == Material.GOLDEN_APPLE || event.getItemDrop().getItemStack().getType() == Material.IRON_SWORD || event.getItemDrop().getItemStack().getType() == Material.DIAMOND_PICKAXE || event.getItemDrop().getItemStack().getType() == Material.STAINED_CLAY) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent event)
    {
        if(event.getSlotType() == InventoryType.SlotType.ARMOR)
        {
            event.setCancelled(true);
        }
    }


}
