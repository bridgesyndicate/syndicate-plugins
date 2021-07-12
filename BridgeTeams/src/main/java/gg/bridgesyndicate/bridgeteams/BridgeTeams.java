package gg.bridgesyndicate.bridgeteams;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;

import static com.avaje.ebean.Ebean.update;

public final class BridgeTeams extends JavaPlugin implements Listener{

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents((Listener) this, (Plugin) this);
        Team.clearTeams();
    }

    @EventHandler
    public void onDeathOfPlayer(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);

        Location redLoc = new Location(Bukkit.getWorld("world"), 28.5, 98, 0.5, 90, 0);
        Location blueLoc = new Location(Bukkit.getWorld("world"), -27.5, 98, 0.5, -90, 0);

        event.getDrops().clear();
        player.getInventory().clear();

        for(PotionEffect effect:player.getActivePotionEffects()){
            player.removePotionEffect(effect.getType());
        }

        Team.getTeamType(player);
        if(Team.getTeamType(player) == TeamType.BLUE){
            player.teleport(blueLoc);
            Inventory.blueInv(player);
        }
        else{
            player.teleport(redLoc);
            Inventory.redInv(player);
        }

        String killed = event.getEntity().getName();
        String killer = event.getEntity().getKiller().getName();
        if(Team.getTeamType(player) == TeamType.BLUE){
            event.setDeathMessage(ChatColor.BLUE + killed + ChatColor.GRAY + " was killed by " + ChatColor.RED + killer + ChatColor.GRAY + ".");
        }
        else{

            event.setDeathMessage(ChatColor.RED + killed + ChatColor.GRAY + " was killed by " + ChatColor.BLUE + killer + ChatColor.GRAY + ".");
        }
        event.setDroppedExp(0);

    }

    @EventHandler
    public void playerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        String killed = player.getName();
        if (player.getLocation().getY() < 83) {
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20);

            Location redLoc = new Location(Bukkit.getWorld("world"), 28.5, 98, 0.5, 90, 0);
            Location blueLoc = new Location(Bukkit.getWorld("world"), -27.5, 98, 0.5, -90, 0);

            for(PotionEffect effect:player.getActivePotionEffects()){
                player.removePotionEffect(effect.getType());
            }

            Team.getTeamType(player);
            if(Team.getTeamType(player) == TeamType.BLUE){
                player.teleport(blueLoc);
                Inventory.blueInv(player);
            }
            else{
                player.teleport(redLoc);
                Inventory.redInv(player);
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if(Team.getTeamType(player) == TeamType.BLUE){
                    p.sendMessage(ChatColor.BLUE + killed + ChatColor.GRAY + " fell into the void.");
                }
                else{
                    p.sendMessage(ChatColor.RED + killed + ChatColor.GRAY + " fell into the void.");
                }

            }

        }


    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        ScoreboardManager sm = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = sm.getNewScoreboard();
        if (label.equalsIgnoreCase("assign")) {
            int i = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (i < Bukkit.getOnlinePlayers().toArray().length / 2) {
                    Team.addToTeam(TeamType.RED, player);
                    org.bukkit.scoreboard.Team Red = scoreboard.registerNewTeam("Red");
                    player.setScoreboard(scoreboard);
                    Red.addEntry(player.getName());
                    Red.setPrefix(ChatColor.RED + "");
                    Red.setNameTagVisibility(NameTagVisibility.ALWAYS);

                } else {

                    Team.addToTeam(TeamType.BLUE, player);
                    org.bukkit.scoreboard.Team Blue = scoreboard.registerNewTeam("Blue");
                    player.setScoreboard(scoreboard);
                    Blue.addEntry(player.getName());
                    Blue.setPrefix(ChatColor.BLUE + "");
                    Blue.setNameTagVisibility(NameTagVisibility.ALWAYS);

                }


                i++;
            }

        }
        if(label.equalsIgnoreCase("myteam")){
            sender.sendMessage(Team.getTeamType(((Player)sender)).name());
        }
        return true;

    }

    public void onDisable(){
        Team.clearTeams();
    }
}





