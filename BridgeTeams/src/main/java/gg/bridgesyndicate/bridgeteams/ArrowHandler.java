package gg.bridgesyndicate.bridgeteams;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public final class ArrowHandler implements Listener {
    private final BridgeTeams parentPlugin;

    public ArrowHandler(BridgeTeams parentPlugin) {
        this.parentPlugin = parentPlugin;
    }

    public static HashMap<UUID, Boolean> arrowCooldown = new HashMap<UUID, Boolean>();
    private final int ARROW_COOLDOWN_TIME_IN_MILLIS = 3500;

    @EventHandler // gives the player their arrow back after 3.5 seconds
    public void onEntityShootBowEvent(final EntityShootBowEvent event) {

        Player player = (Player) event.getEntity();
        Arrow arrow = (Arrow) event.getProjectile();
        Vector directionLooking = player.getLocation().getDirection();
        double speed = arrow.getVelocity().length();
        Vector newVelocity = directionLooking.multiply(speed);
        arrow.setVelocity(newVelocity);
        arrow.setCritical(false);
        beginArrowCooldown(player);
        new BukkitRunnable() {
            int ticksSinceShootBow = 0;
            public void run() {
                if (isArrowOnCooldown(player)){
                    if (ticksSinceShootBow < (ARROW_COOLDOWN_TIME_IN_MILLIS/50)) {
                        ticksSinceShootBow++;
                        player.setExp(1-ticksSinceShootBow / (ARROW_COOLDOWN_TIME_IN_MILLIS/50F));
                        player.setLevel(4-(int)Math.floor((ticksSinceShootBow+10) / 20F));
                    } else{
                        parentPlugin.getInventory().reload(player);
                        player.setLevel(0);
                        cancelArrowCooldown(player);
                    }
                } else { // called when they die
                    this.cancel();
                }
            }
        }.runTaskTimer(parentPlugin, 0, 1);
    }

    public boolean isArrowOnCooldown(Player player){
        return arrowCooldown.get(player.getUniqueId());
    }

    public void beginArrowCooldown(Player player){
        arrowCooldown.put(player.getUniqueId(), true);
    }
    public void cancelArrowCooldown(Player player){
        arrowCooldown.put(player.getUniqueId(), false);
    }

    @EventHandler // removes arrows sticking onto players visually
    public void ProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            arrow.remove();
        }
    }

    @EventHandler // removes ability to hit yourself with an arrow
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof Player) {
            Arrow arrow = (Arrow) event.getDamager();
            Player shooter = (Player) arrow.getShooter();
            Player victim = (Player) event.getEntity();
            if (victim.equals(shooter)) {
                event.setCancelled(true);
            }
        }
    }
}
