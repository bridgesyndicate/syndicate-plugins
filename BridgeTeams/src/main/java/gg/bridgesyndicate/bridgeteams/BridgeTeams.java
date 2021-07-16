package gg.bridgesyndicate.bridgeteams;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.List;

public final class BridgeTeams extends JavaPlugin implements Listener{

    @Override
    public void onEnable() {
        System.out.println( this.getClass() + " is loading." );
        this.getServer().getPluginManager().registerEvents(this, this);
        Team.clearTeams();
    }

    @EventHandler
    public void onDeathOfPlayer(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);

        event.getDrops().clear();
        player.getInventory().clear();

        for(PotionEffect effect:player.getActivePotionEffects()){
            player.removePotionEffect(effect.getType());
        }

        Team.getTeam(player);
        player.teleport(Team.getSpawnLocation(player));
        Inventory.setInventory(player);

        String killed = event.getEntity().getName();
        String killer = event.getEntity().getKiller().getName();

        String deathMessage = Team.getChatColor(player).toString() +
                killed +
                ChatColor.GRAY + " was killed by " +
                Team.getChatColor(player) + killer +
                ChatColor.GRAY + ".";
        event.setDeathMessage(deathMessage);
        event.setDroppedExp(0);
    }

    public void playerInVoid(Player player) {
        String killed = player.getName();

        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.teleport(Team.getSpawnLocation(player));
        Inventory.setInventory(player);

        String voidMessage = Team.getChatColor(player) + killed +
                ChatColor.GRAY + " fell into the void.";
        Bukkit.broadcastMessage(voidMessage);
    }

    public void toteScore(Player player, GoalMeta goal){
        player.sendMessage(Team.getTeam(player).toString() + " team entered the " + goal.getGoalName());
        Score score = Score.getInstance();
        if ( Team.getTeam(player) != goal.getTeam() ) {
            score.increment(Team.getTeam(player));
        }
        sendPlayersToCages();
        score.printScore();
    }

    private void sendPlayersToCages() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.teleport(Team.getCageLocation(player));
            Inventory.setInventory(player);
        }
    }

    public void checkForGoal(Player player){
        final List<GoalMeta> goalList = new ArrayList<>();
        goalList.add(Team.getBlueGoalMeta());
        goalList.add(Team.getRedGoalMeta());
        for (GoalMeta goal : goalList) {
            if (goal.getBoundingBox().contains(
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ())
            ) {
                toteScore(player, goal);
            }
        }
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.getLocation().getY() < 83) {
            playerInVoid(player);
        } else {
            checkForGoal(player);
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
            sender.sendMessage(Team.getTeam(((Player)sender)).name());
        }
        return true;

    }

    public void onDisable(){
        Team.clearTeams();
    }
}



