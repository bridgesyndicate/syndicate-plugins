package gg.bridgesyndicate.bridgeteams;

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
        Player damager = (Player) event.getDamager();

        if (MatchTeam.onSameTeam(victim, damager)) {
            event.setCancelled(true);
            return;
        }

        BridgeTeams.lastHitTimestampInMillis.put(damager.getUniqueId(), System.currentTimeMillis());

        // the cooldown for hitting someone is 10 ticks
        // getNoDamageTicks() is (cooldown + 10)
        // if (getNoDamageTicks() - 10) is above cooldown (10), then they must have spawn protection
        if ((damager.getNoDamageTicks() - 10) > 10) {
            damager.setNoDamageTicks(0); // when the damager hits someone, cancel their spawn protection
        }

    }

}
