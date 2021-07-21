package gg.bridgesyndicate.bridgeteams;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class BridgeTeams extends JavaPlugin implements Listener {

    private static final int MAX_BLOCKS = 10000;
    static WorldEditPlugin worldEditPlugin;
    private static final HashMap<UUID, Scoreboard> scoreboards = new HashMap<UUID, Scoreboard>();
    private static final HashMap<UUID, Integer> kills = new HashMap<UUID, Integer>();
    public static final HashMap<UUID, Integer> goals = new HashMap<UUID, Integer>();
    public static int timeLeft = 900;


    @Override
    public void onEnable() {
        System.out.println(this.getClass() + " is loading.");
        this.getServer().getPluginManager().registerEvents(this, this);
        worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        Team.clearTeams();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        setBoard(player);
    }

    public void setBoard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        UUID id = player.getUniqueId();

        scoreboards.put(player.getUniqueId(), board);
        player.setScoreboard(scoreboards.get(id));

        Objective title = board.registerNewObjective("title", "dummy");
        title.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "BRIDGE");
        title.setDisplaySlot(DisplaySlot.SIDEBAR);

        org.bukkit.scoreboard.Team Red = board.registerNewTeam("Red");
        Red.setPrefix(ChatColor.RED + "");
        Red.setNameTagVisibility(NameTagVisibility.ALWAYS);

        org.bukkit.scoreboard.Team Blue = board.registerNewTeam("Blue");
        Blue.setPrefix(ChatColor.BLUE + "");
        Blue.setNameTagVisibility(NameTagVisibility.ALWAYS);

        org.bukkit.scoreboard.Team kill = board.registerNewTeam("kills");
        kill.setSuffix("0");
        kill.addEntry("Kills: §a");
        title.getScore("Kills: §a").setScore(8);

        org.bukkit.scoreboard.Team timer = board.registerNewTeam("timer");
        timer.addEntry("Time Left: §a");
        timer.setSuffix("15:00");
        timer.setPrefix("");
        title.getScore("Time Left: §a").setScore(13);

        Score date = title.getScore(ChatColor.GRAY + "--/--/--   " + ChatColor.DARK_GRAY + "" + player.getName());
        date.setScore(15);

        Score blank1 = title.getScore("§1");
        blank1.setScore(14);

        Score blank2 = title.getScore("§2");
        blank2.setScore(12);

        Score bluegoals = title.getScore(ChatColor.BLUE + "[B] " + ChatColor.GRAY + "⬤⬤⬤⬤⬤");
        bluegoals.setScore(11);
        Score redgoals = title.getScore(ChatColor.RED + "[R] " + ChatColor.GRAY + "⬤⬤⬤⬤⬤");
        redgoals.setScore(10);

        Score blank3 = title.getScore("§3");
        blank3.setScore(9);

        org.bukkit.scoreboard.Team goal = board.registerNewTeam("goals");
        goal.setSuffix("0");
        goal.addEntry("Goals: §a");
        title.getScore("Goals: §a").setScore(7);

        Score blank4 = title.getScore("§4");
        blank4.setScore(6);

        Score mode = title.getScore(ChatColor.WHITE + "Mode: " + ChatColor.GREEN + "The Bridge Duel");
        mode.setScore(5);
        Score dailyStreak = title.getScore(ChatColor.WHITE + "Daily Streak: " + ChatColor.GREEN + "999");
        dailyStreak.setScore(4);
        Score bestDailyStreak = title.getScore(ChatColor.WHITE + "Best Daily Streak: " + ChatColor.GREEN + "999");
        bestDailyStreak.setScore(3);

        Score blank5 = title.getScore("§5");
        blank5.setScore(2);

        Score server = title.getScore(ChatColor.YELLOW + "localhost");
        server.setScore(1);

        if (board.getObjective("health") != null) {
            board.getObjective("health").unregister();
        }
        Objective o = board.registerNewObjective("health", "health");
        o.setDisplayName(ChatColor.RED + "❤");
        o.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    @EventHandler
    public void onDeathOfPlayer(PlayerDeathEvent event) {
        Player player = event.getEntity();
        sendDeadPlayerToSpawn(player);
        Player killer = player.getKiller();
        Scoreboard killerboard = killer.getScoreboard();
        killerboard.getTeam("kills").setSuffix("");

        String killerString = killer.getName();
        String killedString = player.getName();

        UUID killerId = killer.getUniqueId();
        int newKills = kills.merge(killerId, 1, (oldKills, ignore) -> oldKills + 1);

        killerboard.getTeam("kills").setSuffix("" + newKills);

        String deathMessage = Team.getChatColor(player).toString() +
                killedString +
                ChatColor.GRAY + " was killed by " +
                Team.getChatColor(killer) + killerString +
                ChatColor.GRAY + ".";
        event.setDeathMessage(deathMessage);
        event.setDroppedExp(0);

        killer.playSound(killer.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);
    }

    private void sendDeadPlayerToSpawn(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setNoDamageTicks(50);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.teleport(Team.getSpawnLocation(player));

        Vector v = player.getVelocity();
        v.setX(0);
        v.setY(0);
        v.setZ(0);
        player.setVelocity(v);
        new BukkitRunnable() {
            @Override
            public void run() {
                v.setX(0);
                v.setY(0);
                v.setZ(0);
                player.setVelocity(v);
            }
        }.runTaskLater(this, 1);


        Inventory.setInventory(player);
        player.playSound(player.getLocation(), Sound.HURT_FLESH, 1.0f, 0.9f);
    }

    public void playerInVoid(Player player) {
        String killed = player.getName();
        sendDeadPlayerToSpawn(player);
        String voidMessage = Team.getChatColor(player) + killed +
                ChatColor.GRAY + " fell into the void.";
        Bukkit.broadcastMessage(voidMessage);
    }

    public void toteScore(Player player, GoalMeta goal) {
        player.sendMessage(Team.getTeam(player).toString() + " team entered the " + goal.getGoalName());

        GameScore score = GameScore.getInstance();
        if (Team.getTeam(player) != goal.getTeam()) {
            score.increment(Team.getTeam(player));

            UUID ScorerId = player.getUniqueId();
            int newGoals = goals.merge(ScorerId, 1, (oldGoals, ignore) -> oldGoals + 1);

            if (Team.getTeam(player) == TeamType.RED) {
                ChatBroadcasts.redScoreMessage(player);
            } else {
                ChatBroadcasts.blueScoreMessage(player);
            }

            Scoreboard board = player.getScoreboard();
            board.getTeam("goals").setSuffix("" + newGoals);
        }

        buildCages();
        sendPlayersToCages();

    }

    private Location getOrigin() {
        return (new Location(Bukkit.getWorld("world"), 0, 0, 0, 0, 0));
    }

    private void buildCages() {
        EditSession editSession = new EditSession(new BukkitWorld(getOrigin().getWorld()), MAX_BLOCKS);
        editSession.enableQueue();
        for (TeamType team : Team.getTeams()) {
            Location cageLocation = Team.getCageLocation(team);
            final File schematic = new File("C:/Users/benal/Desktop/spigot/plugins/schematics/mushroomcage.schematic");
            try {
                SchematicFormat schematicFormat = SchematicFormat.getFormat(schematic);
                CuboidClipboard clipboard = schematicFormat.load(schematic);
                clipboard.paste(editSession, BukkitUtil.toVector(cageLocation), true);
            } catch (MaxChangedBlocksException | DataException | IOException e) {
                e.printStackTrace();
            }
        }
        editSession.flushQueue();
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("FIGHT!");
                editSession.undo(editSession);
            }
        }, 100);
    }

    private void sendPlayersToCages() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.teleport(Team.getCagePlayerLocation(player));
            Inventory.setInventory(player);


        }

    }

    public void checkForGoal(Player player) {
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
    public void onBowHit(EntityDamageByEntityEvent event) {

        Entity entityHitter = event.getDamager();

        if (entityHitter instanceof Arrow) {
            Player shoota = (Player) entityHitter;
            shoota.playSound(shoota.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f);
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


    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (label.equalsIgnoreCase("assign")) {
            if (commandSender instanceof Player) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (timeLeft > 0) {
                            timeLeft--;
                            String formattedTime = formatTime(timeLeft);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                org.bukkit.scoreboard.Team timer = player.getScoreboard().getTeam("timer");
                                timer.setSuffix("" + formattedTime);
                            }
                        } else {
                            cancel();
                        }
                    }
                }.runTaskTimer(this, 0, 20);
            }

            int i = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (i < Bukkit.getOnlinePlayers().toArray().length / 2) {
                    Team.addToTeam(TeamType.RED, player);
                    Scoreboard board = player.getScoreboard();
                    org.bukkit.scoreboard.Team Red = board.getTeam("Red");
                    Red.addEntry(player.getName());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            List<String> blueTeam = Team.getBlueTeam();
                            String blueTeamString = blueTeam.toString().replace("[", "").replace("]", "");
                            org.bukkit.scoreboard.Team Blue = board.getTeam("Blue");
                            Blue.addEntry(blueTeamString);
                        }
                    }.runTaskLater(this, 1);
                } else {
                    Team.addToTeam(TeamType.BLUE, player);
                    Scoreboard board = player.getScoreboard();
                    org.bukkit.scoreboard.Team Blue = board.getTeam("Blue");
                    Blue.addEntry(player.getName());
                    List<String> redTeam = Team.getRedTeam();
                    String redTeamString = redTeam.toString().replace("[", "").replace("]", "");
                    org.bukkit.scoreboard.Team Red = board.getTeam("Red");
                    Red.addEntry(redTeamString);
                }
                i++;
            }
        }

        if (label.equalsIgnoreCase("myteam")) {
            commandSender.sendMessage(Team.getTeamName((Player) commandSender));
        }
        return true;

    }

    public static String formatTime(int sc) {
        if (sc <= 0) return "0:00";
        int m = sc % 3600 / 60;
        int s = sc % 60;
        if (s < 10) {
            return m + ":0" + s;
        } else {
            return m + ":" + s;
        }
    }

    public void onDisable(){
        Team.clearTeams();
    }
}



