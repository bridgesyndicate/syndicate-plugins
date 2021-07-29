package gg.bridgesyndicate.bridgeteams;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class BridgeTeams extends JavaPlugin implements Listener {

    private static final int MAX_BLOCKS = 10000;
    static WorldEditPlugin worldEditPlugin;
    private static final HashMap<UUID, Scoreboard> scoreboards = new HashMap<UUID, Scoreboard>();
    private static final HashMap<UUID, Integer> kills = new HashMap<UUID, Integer>();
    private static final  HashMap<UUID, String> lastdamager = new HashMap<UUID, String>();
    public static final HashMap<UUID, Integer> goals = new HashMap<UUID, Integer>();
    public static int timeLeft = 900;
    private static String scorer = null;


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
        resetPlayer(player);
        setBoard(player);


    }

    public void resetPlayer(Player player){
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setNoDamageTicks(50);
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        Vector v = player.getVelocity();
        v.setX(0);
        v.setY(0);
        v.setZ(0);

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

        Objective j = board.registerNewObjective("health2","health");
        j.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    @EventHandler
    public static void onPlayerChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        String msg = event.getMessage();
        event.setCancelled(true);
        Bukkit.broadcastMessage(ChatColor.GREEN + "[GAME] " + ChatColor.RED + "[ADMIN] " + player.getName() + ChatColor.WHITE + ": " + msg);
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

    @EventHandler
    public void cancelBlockPlacement(BlockPlaceEvent event){

        Player player = event.getPlayer();
        Block b = event.getBlock();
        int bX = b.getX();
        int bY = b.getY();
        int bZ = b.getZ();

        if(bX > 25 || bX < -25){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't place blocks there!");
            return;
        }
        if(bY > 99 || bY < 84){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't place blocks there!");
            return;
        }
        if(bZ > 20 || bZ < -20){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't place blocks there!");
            return;
        }
    }

    @EventHandler
    public void cancelBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Block b = event.getBlock();
        int bX = b.getX();
        int bY = b.getY();
        int bZ = b.getZ();

        if (event.getBlock().getType() != Material.STAINED_CLAY) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't break that block!");
            return;
        }
        if (bY > 99 || bY < 84 || bX > 25 || bX < -25 || bZ > 20 || bZ < -20){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't break that block!");
            return;
        }
        if(bY == 99 && (bX == 25 || bX == -25) && (bZ == 4 || bZ == -4)){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't break that block!");
            return;
        }
    }

    @EventHandler
    public void placeBlocksInCage(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(player.getGameMode() == GameMode.ADVENTURE){
            if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() ==  Action.RIGHT_CLICK_BLOCK){
                if(player.getItemInHand().getType() == Material.STAINED_CLAY){
                    player.sendMessage(ChatColor.RED + "You can't place blocks there!");
                    player.playSound(player.getLocation(), Sound.DIG_STONE, 1.0f, 0.8f);
                }
            }
        }
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

    public void toteScore(Player player, GoalMeta goal) {

        GameScore score = GameScore.getInstance();
        if (Team.getTeam(player) != goal.getTeam()) {
            score.increment(Team.getTeam(player));
            scorer = "" + Team.getChatColor(player) + "" + player.getName() + " scored!";

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
        spawnFireworks(player);

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
                Bukkit.broadcastMessage("");
                for(Player player : Bukkit.getOnlinePlayers()){
                    player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1.0f, 1.0f);
                    player.setGameMode(GameMode.SURVIVAL);
                }
                editSession.undo(editSession);
            }
        }, 100);

    }

    public void sendTitles(Player player){

        String scorerName;

        if(scorer != null){
            scorerName = scorer;
        }
        else{
            scorerName = "";
        }

        Title five = new Title("" + scorerName,"&7Cages open in: &a5s&7...",0,3,0);
        five.send(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                Title four = new Title("" + scorerName,"&7Cages open in: &a4s&7...",0,3,0);
                four.send(player);
                player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f);
            }
        }.runTaskLater(this, 20);
        new BukkitRunnable() {
            @Override
            public void run() {
                Title three = new Title("" + scorerName,"&7Cages open in: &a3s&7...",0,3,0);
                three.send(player);
                player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f);
            }
        }.runTaskLater(this, 40);
        new BukkitRunnable() {
            @Override
            public void run() {
                Title two = new Title("" + scorerName,"&7Cages open in: &a2s&7...",0,3,0);
                two.send(player);
                player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f);
            }
        }.runTaskLater(this, 60);
        new BukkitRunnable() {
            @Override
            public void run() {
                Title one = new Title("" + scorerName,"&7Cages open in: &a1s&7...",0,3,0);
                one.send(player);
                player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f);
            }
        }.runTaskLater(this, 80);
        new BukkitRunnable() {
            @Override
            public void run() {
                Title fight = new Title("","&aFight!",0,1,1);
                fight.send(player);
            }
        }.runTaskLater(this, 100);

    }

    private void spawnFireworks(Player player){


        sendFirework(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                sendFirework(player);
            }
        }.runTaskLater(this, 20);
        new BukkitRunnable() {
            @Override
            public void run() {
                sendFirework(player);
            }
        }.runTaskLater(this, 40);
        new BukkitRunnable() {
            @Override
            public void run() {
                sendFirework(player);
            }
        }.runTaskLater(this, 60);
        new BukkitRunnable() {
            @Override
            public void run() {
                sendFirework(player);
            }
        }.runTaskLater(this, 80);

    }

    private void sendFirework(Player player) {

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
        if (Team.getTeam(player) == TeamType.RED) {
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
        }.runTaskLater(this, 2);

    }

    private void sendPlayersToCages() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(Team.getCagePlayerLocation(player));
            player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f);
            sendTitles(player);
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20);
            Inventory.setInventory(player);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

        }

    }

    public void checkForGoal(Player player) {

        final List<GoalMeta> goalList = new ArrayList<>();
        goalList.add(Team.getBlueGoalMeta());
        goalList.add(Team.getRedGoalMeta());



            for (GoalMeta goal : goalList) {
                if (Team.getTeam(player) != goal.getTeam()) {

                    if (goal.getBoundingBox().contains(
                        player.getLocation().getX(),
                        player.getLocation().getY(),
                        player.getLocation().getZ()))
                    {
                        toteScore(player, goal);
                    }
                }
            }
        }


    @EventHandler
    public void onBowHit(EntityDamageByEntityEvent event) {

        Entity entityVictim = event.getEntity();
        Entity entityHitter = event.getDamager();

        if (entityHitter instanceof Arrow && entityVictim instanceof Player) {

            Arrow arrow = (Arrow) entityHitter;
            if(arrow.getShooter() instanceof Player) {
                Player shoota = (Player) arrow.getShooter();
                Player shot = (Player) entityVictim;

                double shotHealth = shot.getHealth();
                DecimalFormat format = new DecimalFormat("##.#");
                String heartValue = format.format(shotHealth);

                String shotName = shot.getName();
                shoota.sendMessage(ChatColor.GRAY + shotName + " is on " + ChatColor.RED + heartValue + ChatColor.GRAY + " HP!");
                shoota.playSound(shoota.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f);

                CraftPlayer craft = (CraftPlayer) shot;
                float absFloat = craft.getHandle().getAbsorptionHearts();
                int absInt = (int) absFloat;

                int health = (int)(shot.getHealth() - event.getFinalDamage())/2;
                double dmgD = event.getFinalDamage()/2;
                int dmg = (int) Math.round(dmgD);
                int goneHealth = 10 - (health + dmg);

                String damage = ActionBarHealth.formatDamage(dmg);
                String hearts = ActionBarHealth.formatHealth(health);
                String blackHearts = ActionBarHealth.formatBlackHearts(goneHealth);
                String goldHearts = ActionBarHealth.formatGoldHearts(absInt);

                if(health >= 0){
                    if(absInt > 0){

                        ActionBarHealth showHearts = new ActionBarHealth(Team.getChatColor(shot) + "" + shotName + " "
                                + hearts + goldHearts);
                        showHearts.sendToPlayer(shoota);

                    }else{

                        ActionBarHealth showHearts = new ActionBarHealth(Team.getChatColor(shot) + "" + shotName + " "
                                + hearts + damage + blackHearts);
                        showHearts.sendToPlayer(shoota);

                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        Entity entityVictim = event.getEntity();
        Entity entityHitter = event.getDamager();

        if (entityHitter instanceof Player && entityVictim instanceof Player) {

            Player hitter = (Player) entityHitter;
            Player hit = (Player) entityVictim;

            CraftPlayer craft = (CraftPlayer) hit;
            float absFloat = craft.getHandle().getAbsorptionHearts();
            int absInt = (int) absFloat;

            String hitName = hit.getName();
            int health = (int)(hit.getHealth() - event.getFinalDamage())/2;
            double dmgD = event.getFinalDamage()/2;
            int dmg = (int) Math.round(dmgD);
            int goneHealth = 10 - (health + dmg);

            String damage = ActionBarHealth.formatDamage(dmg);
            String hearts = ActionBarHealth.formatHealth(health);
            String blackHearts = ActionBarHealth.formatBlackHearts(goneHealth);
            String goldHearts = ActionBarHealth.formatGoldHearts(absInt);

            if(health >= 0){
                if(absInt > 0){

                    ActionBarHealth showHearts = new ActionBarHealth(Team.getChatColor(hit) + "" + hitName + " "
                            + hearts + goldHearts);
                    showHearts.sendToPlayer(hitter);

                }else{

                    ActionBarHealth showHearts = new ActionBarHealth(Team.getChatColor(hit) + "" + hitName + " "
                            + hearts + damage + blackHearts);
                    showHearts.sendToPlayer(hitter);

                }
            }
        }
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent e) {

        Player player = e.getPlayer();

        if (player.getLocation().getY() < 83) {
            if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {

                Entity ed = ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();
                if (ed instanceof Player) {

                    final Player damager = (Player) ed;
                    final String killed = player.getName();
                    final String killer = damager.getName();

                    UUID killerId = damager.getUniqueId();
                    int newKills = kills.merge(killerId, 1, (oldKills, ignore) -> oldKills + 1);

                    Scoreboard board = damager.getScoreboard();
                    board.getTeam("kills").setSuffix("" + newKills);

                    Bukkit.broadcastMessage(Team.getChatColor(player) + killed + ChatColor.GRAY + " was hit into the void by "
                            + Team.getChatColor(damager) + killer + ChatColor.GRAY + ".");

                    damager.playSound(damager.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);

                }

            } else {
                String killed = player.getName();
                Bukkit.broadcastMessage(Team.getChatColor(player) + killed + ChatColor.GRAY + " fell into the void.");
            }

            sendDeadPlayerToSpawn(player);

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
            buildCages();
            sendPlayersToCages();
            for(Player player : Bukkit.getOnlinePlayers()){
                String opponent;
                if(Team.getTeam(player) == TeamType.RED){
                    List<String> blueTeam = Team.getBlueTeam();
                    opponent = blueTeam.toString().replace("[", "").replace("]", "");
                    ChatBroadcasts.gameStartMessage(player, opponent);
                }else{
                    List<String> redTeam = Team.getRedTeam();
                    opponent = redTeam.toString().replace("[", "").replace("]", "");
                    ChatBroadcasts.gameStartMessage(player, opponent);
                }

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



