package gg.bridgesyndicate.bridgeteams;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class BridgeFireworks {
    Plugin parentPlugin = null;

    public BridgeFireworks(Plugin parentPlugin) {
        this.parentPlugin = parentPlugin;
    }

    public void spawnFireworks(Player player) {

        new BukkitRunnable() {
            int secondsOfFireworks = 5;

            public void run() {
                if (secondsOfFireworks > 0) {
                    sendFirework(player);
                    secondsOfFireworks--;
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(parentPlugin, 0, 20);
    }

    public void sendFirework(Player player) {

        Location l = new Location(Bukkit.getWorld("world"), 0.5, 97, 0.5);

        int randomX = ThreadLocalRandom.current().nextInt(-10, 10 + 1);
        int randomY = ThreadLocalRandom.current().nextInt(95, 104 + 1);
        int randomZ = ThreadLocalRandom.current().nextInt(-10, 10 + 1);
        l.setX(randomX);
        l.setY(randomY);
        l.setZ(randomZ);

        Random r = new Random();

        int rt = r.nextInt(5) + 1;
        FireworkEffect.Type type = FireworkEffect.Type.BALL;
        if (rt == 2) type = FireworkEffect.Type.BALL_LARGE;
        if (rt == 3) type = FireworkEffect.Type.BURST;
        if (rt == 4) type = FireworkEffect.Type.CREEPER;
        if (rt == 5) type = FireworkEffect.Type.STAR;

        Firework fw = (Firework) Bukkit.getWorld("world").spawnEntity(l, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.setPower(0);
        if (MatchTeam.getTeam(player) == TeamType.RED) {
            fwm.addEffect(FireworkEffect.builder().flicker(true).withColor(Color.RED).withFade(Color.WHITE).with(type).build());
        } else {
            fwm.addEffect(FireworkEffect.builder().flicker(true).withColor(Color.BLUE).withFade(Color.WHITE).with(type).build());
        }
        fw.setFireworkMeta(fwm);

        new BukkitRunnable() {
            @Override
            public void run() {
                fw.detonate();
            }
        }.runTaskLater(parentPlugin, 2);
    }
}
