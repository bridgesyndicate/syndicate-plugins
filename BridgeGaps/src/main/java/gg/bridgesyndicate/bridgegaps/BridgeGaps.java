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

public final class BridgeGaps extends JavaPlugin implements Listener {
    private Object PlayerInteractEvent;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents((Listener) this, (Plugin) this);
    }

    @EventHandler
    public void onPlayerClick(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() == null)
            return;

        if (Objects.requireNonNull(player.getItemInHand()).getType() == Material.GOLDEN_APPLE) {

            event.setCancelled(true);
            player.setHealth(20);

            if (!player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1200, 0, false, false));
            }

            for (ItemStack item : player.getInventory()) {
                if (item != null) { // HOW is a null getting into the inventory?
                    if (item.getType().equals(Material.GOLDEN_APPLE)) {

                        int itemAmount = item.getAmount();

                        if (item.getAmount() == 1) {
                            player.getInventory().remove(Material.GOLDEN_APPLE);
                        } else {
                            int currentAmount = itemAmount - 1;
                            item.setAmount(currentAmount);
                            ItemStack stack = new ItemStack(Material.GOLDEN_APPLE, currentAmount);
                            player.setItemInHand(stack);
                        }

                    }
                }
            }
        }
    }
}