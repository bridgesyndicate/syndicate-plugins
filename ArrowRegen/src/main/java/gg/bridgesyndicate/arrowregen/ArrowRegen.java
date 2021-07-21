package gg.bridgesyndicate.arrowregen;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public final class ArrowRegen extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        System.out.println(this.getClass() + " is loading.");
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void reload(Player player) {
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta itemMetaArrow = arrow.getItemMeta();
        itemMetaArrow.setDisplayName(ChatColor.GREEN + "Arrow");

        ArrayList<String> arrowLore = new ArrayList<>();
        arrowLore.add(ChatColor.GRAY + "Regenerates every " + ChatColor.GREEN + "3.5s" + ChatColor.GRAY + "!");
        itemMetaArrow.setLore(arrowLore);

        Player p = player.getServer().getPlayer(player.getUniqueId());
        arrow.setItemMeta(itemMetaArrow);
        p.getInventory().setItem(8, arrow);
        p.updateInventory();
    }

    @EventHandler
    public void onEntityShootBowEvent(final EntityShootBowEvent event) {
        Player player = (Player) event.getEntity();

        new BukkitRunnable() {
            int ticksSinceShootBow = 0;

            public void run() {
                if (ticksSinceShootBow < 70) {
                    ticksSinceShootBow++;
                    player.setExp(1-ticksSinceShootBow / 70F);
                    player.setLevel(4-(int)Math.floor(ticksSinceShootBow / 20F));
                } else {
                    reload(player);
                    player.setLevel(0);
                    this.cancel();
                }
            }
        }.runTaskTimer(this, 0, 1);
    }

    @EventHandler
    public void ProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            arrow.remove();
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event)
    {
        if(!(event.getDamager() instanceof Arrow)) return;
        if(!(event.getEntity() instanceof Player)) return;

        Arrow arrow = (Arrow) event.getDamager();
        Entity shooter = (Entity) arrow.getShooter();

        if(!(shooter instanceof Player)) return;

        Player player = (Player) event.getEntity();

        if(player.equals(shooter))
        {
            event.setCancelled(true);
        }
    }
}
