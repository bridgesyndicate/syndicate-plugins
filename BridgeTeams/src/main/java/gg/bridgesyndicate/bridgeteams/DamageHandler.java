package gg.bridgesyndicate.bridgeteams;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageHandler implements Listener {

    private final BridgeTeams parentPlugin;

    public DamageHandler(BridgeTeams parentPlugin){
        this.parentPlugin = parentPlugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {

        if (!parentPlugin.getGame().isDuringGame()) {
            event.setCancelled(true);
            return;
        }
        Player victim = (Player) event.getEntity();
        Entity entityDamager = event.getDamager();
        Player playerDamager = null;
        if (entityDamager instanceof Player) {
            playerDamager = (Player) entityDamager;
        } else if (entityDamager instanceof Arrow) {
            Arrow arrow = (Arrow) entityDamager;
            playerDamager = (Player) arrow.getShooter();
        }

        if (MatchTeam.onSameTeam(victim, playerDamager)) {
            event.setCancelled(true);
            return;
        }

        BridgeTeams.lastHitTimestampInMillis.put(playerDamager.getUniqueId(), System.currentTimeMillis());

        // the cooldown for hitting someone is 10 ticks
        // getNoDamageTicks() is (cooldown + 10)
        // if (getNoDamageTicks() - 10) is above cooldown (10), then they must have spawn protection
        if ((playerDamager.getNoDamageTicks() - 10) > 10) {
            playerDamager.setNoDamageTicks(0); // when the damager hits someone, cancel their spawn protection
        }

    }

}
