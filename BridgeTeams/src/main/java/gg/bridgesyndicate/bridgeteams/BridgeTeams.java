package gg.bridgesyndicate.bridgeteams;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.parser.ParseException;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class BridgeTeams extends JavaPlugin implements Listener {

    private static final int MAX_BLOCKS = 10000;
    static WorldEditPlugin worldEditPlugin;
    private static final HashMap<UUID, Scoreboard> scoreboards = new HashMap<UUID, Scoreboard>();
    private static final HashMap<UUID, Integer> kills = new HashMap<UUID, Integer>();
    private static final  HashMap<UUID, String> lastdamager = new HashMap<UUID, String>();
    public static final HashMap<UUID, Integer> goals = new HashMap<UUID, Integer>();
    public static int timeLeft = 900;
    private static Game game = null;


    @Override
    public void onEnable() {
        System.out.println(this.getClass() + " is loading.");
        this.getServer().getPluginManager().registerEvents(this, this);
        worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        String jsonFromAwsSqs = "{\n" +
                "  \"blueTeam\": [\n" +
                "    \"vice9\"\n" +
                "  ],\n" +
                "  \"requiredPlayers\": 2,\n" +
                "  \"redTeam\": [\n" +
                "    \"KIIER\"\n" +
                "  ]\n" +
                "}\n";
        JsonParser jsonParser = JsonParser.DEFAULT;
        try {
            game = jsonParser.parse(jsonFromAwsSqs, Game.class);
        } catch (ParseException e) {
            System.err.println("Cannot parse game json.");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if ( game.getState() != Game.GameState.DURING_GAME &&
                game.getState() != Game.GameState.CAGED
        ) {
            e.setCancelled(true);
        } else {
            return;
        }
    }

    public void resetPlayerHealthAndInventory(Player player){
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        setInventoryForPlayer(player);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        zeroPlayerVelocity(player);
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
        player.setNoDamageTicks(50);
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
        resetPlayerHealthAndInventory(player);
        player.teleport(Team.getSpawnLocation(player));
        zeroPlayerVelocity(player);
        player.playSound(player.getLocation(), Sound.HURT_FLESH, 1.0f, 0.9f);
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

    private void zeroPlayerVelocity(Player player) {
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
    }

    public void toteScore(Player player, GoalMeta goal) {
        GameScore score = GameScore.getInstance();
        cagePlayers();
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

    }

    private Location getOrigin() {
        return (new Location(Bukkit.getWorld("world"), 0, 0, 0, 0, 0));
    }

    private void buildCages() {
        EditSession editSession = new EditSession(new BukkitWorld(getOrigin().getWorld()), MAX_BLOCKS);
        editSession.enableQueue();
        for (TeamType team : Team.getTeams()) {
            Location cageLocation = Team.getCageLocation(team);
            final File schematic = new File("/app/minecraft-home/plugins/WorldEdit/schematics/mushroomcage.schematic");
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
                game.setState(Game.GameState.DURING_GAME);
                for(Player player : Bukkit.getOnlinePlayers()){
                    player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1.0f, 1.0f);
                }
                Bukkit.broadcastMessage("FIGHT!");
                editSession.undo(editSession);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    setGameModeForPlayer(player);
                }
            }
        }, 100);
    }

    private void cagePlayers() {
        game.setState(Game.GameState.CAGED);
        buildCages();
        for (Player player : Bukkit.getOnlinePlayers()) {
            resetPlayerHealthAndInventory(player);
            setGameModeForPlayer(player);
            player.teleport(Team.getCagePlayerLocation(player));
        }
    }

    public void checkForGoal(Player player) {
        if ( game.getState() != Game.GameState.DURING_GAME ) return;
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
                if (game.getState() == Game.GameState.DURING_GAME) {
                    String killed = player.getName();
                    Bukkit.broadcastMessage(Team.getChatColor(player) + killed + ChatColor.GRAY + " fell into the void.");
                }
            }

            sendDeadPlayerToSpawn(player);

        } else {
            checkForGoal(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.broadcastMessage("Welcome to the server " + player.getName() + "!");
        setGameModeForPlayer(player);
        resetPlayerHealthAndInventory(player);

        if (game == null) {
            System.err.println("Game not initialized!");
            return;
        }
        // resetPlayer(player);
        // set waiting board
        // cancel death inventory reset, death messages, pvp, and scoring
        if (game.hasPlayer(player)) {
            assignToTeam(player);
            System.out.println("joined players: " + game.getNumberOfJoinedPlayers() + "required players:" + game.getRequiredPlayers());
            if (game.getNumberOfJoinedPlayers() == game.getRequiredPlayers())
                startGame();
        } else {
            makeSpectator(player);
        }
        teleportRejoinedPlayer(player);
    }

    private void teleportRejoinedPlayer(Player player) {
        //teleport to the right place, depending upon game mode
        switch( game.getState() ) {
            case DURING_GAME:
                sendDeadPlayerToSpawn(player);
                break;
            case CAGED:
                player.teleport(Team.getCagePlayerLocation(player));
                break;
            default:
                // do nothing
        }
    }

    private void setInventoryForPlayer(Player player) {
        switch( game.getState() ) {
            case BEFORE_GAME:
            case AFTER_GAME:
                player.getInventory().clear();
                break;
            case DURING_GAME:
                Inventory.setDefaultInventory(player);
                break;
            default:
                // do nothing
        }
    }

    private void setGameModeForPlayer(Player player) {
        switch( game.getState() ) {
            case BEFORE_GAME:
            case CAGED:
                player.setGameMode(GameMode.ADVENTURE);
                break;
            case DURING_GAME:
                player.setGameMode(GameMode.SURVIVAL);
                break;
            case AFTER_GAME:
                player.setGameMode(GameMode.SPECTATOR);
                break;
            default:
                // do nothing
        }
    }

    private void startGame() {
        System.out.println("startGame()");
        game.setState(Game.GameState.DURING_GAME);

        for (TeamType team : Team.getTeams()) {
            for (String playerName : Team.getPlayers(team)) {
                Player player = Bukkit.getPlayer(playerName);
                TeamType opposingTeam = Team.getOpposingTeam(team);
                String opponentNames = String.join(", ", Team.getPlayers(opposingTeam));
                ChatBroadcasts.gameStartMessage(player, opponentNames);
            }
        }
    }


    //display opponent and start message in chat
        // for each player
        //   send chat message with list of other teams players
    //set all player scoreboards
        // for each player
        //   set universal and personal scoreboard values
    //set all player inventories
    //set holograms at each goal location
        //for each player
        //  set armor color and block color respective to their team
    //reset all player healths, saturation, etc
    //build team cages and send teams to respective cages
    //start cage countdown and title countdown
        //for each player
        //  start the cage title countdown sequence
        //  on zero cage is undone
    //start game countdown
        //for each player
        // update scoreboard to start counting the 15 minutes down
    private void makeSpectator(Player player) {
    }

    private void assignToTeam(Player player) {
        TeamType teamColor = game.getTeam(player);
        Team.addToTeam(teamColor, player);
//        Scoreboard board = player.getScoreboard();
//        System.out.println("Scoreboard board: " + board);
//        System.out.println("Your scoreboard team is :" + Team.getScoreboardName(player));
//        org.bukkit.scoreboard.Team scoreboardTeamName = board.getTeam(Team.getScoreboardName(player));
//        System.out.println("org.bukkit.scoreboard.Team scoreboardTeamName: " + scoreboardTeamName);
//        scoreboardTeamName.addEntry(player.getName());
        game.playerJoined(player.getName());
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
            cagePlayers();
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



