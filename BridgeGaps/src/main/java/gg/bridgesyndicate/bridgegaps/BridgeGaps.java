package gg.bridgesyndicate.bridgegaps;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public final class BridgeGaps extends JavaPlugin implements Listener{
    private Object PlayerInteractEvent;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents((Listener) this, (Plugin) this);
    }

    @EventHandler
    public void onPlayerClick(PlayerItemConsumeEvent event)
    {
        Player player = event.getPlayer();
        if (event.getItem() == null)
            return;

        if(Objects.requireNonNull(player.getItemInHand()).getType() == Material.GOLDEN_APPLE)
        {
            event.setCancelled(true);
            player.setHealth(20);

            if(player.hasPotionEffect(PotionEffectType.REGENERATION) || player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
                event.setCancelled(true);
            }
            else player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,1200,0,false,false));
            }

        for (ItemStack item : player.getInventory()) {
            if (item.getType().equals(Material.GOLDEN_APPLE)) {
                int itemAmount = item.getAmount();
                if (item.getAmount() == 1) {
                    player.getInventory().remove(Material.GOLDEN_APPLE);
                } else {

                    item.setAmount(itemAmount - 1);
                }
            }
        }

        }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
