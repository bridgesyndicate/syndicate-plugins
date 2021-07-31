package gg.bridgesyndicate.bridgeteams;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.parser.ParseException;
import org.apache.juneau.rest.client.RestCallException;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.rest.client.RestClientBuilder;
import org.apache.juneau.serializer.SerializeException;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;
import sun.net.www.http.HttpClient;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class BridgeTeams extends JavaPlugin implements Listener {

    private static final int MAX_BLOCKS = 10000;
    private static final HashMap<UUID, Scoreboard> scoreboards = new HashMap<UUID, Scoreboard>();
    private static final HashMap<UUID, Integer> kills = new HashMap<UUID, Integer>();
    public static int timeLeft = 900;
    private static Game game = null;

    public enum scoreboardSections {TIMER}

    private static final String TIMER_STRING = "Time Left: " + ChatColor.GREEN;

    private void printWorldRules() {
        for (String gameRule : Bukkit.getWorld("world").getGameRules()) {
            System.out.println(gameRule + " : " + Bukkit.getWorld("world").getGameRuleValue(gameRule));
        }
    }

    @Override
    public void onEnable() {
        System.out.println(this.getClass() + " is loading.");
        this.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getWorld("world").setGameRuleValue("keepInventory", "true");
        Bukkit.getWorld("world").setGameRuleValue("naturalRegeneration", "false");
        Bukkit.getWorld("world").setGameRuleValue("doDaylightCycle", "false");
        Bukkit.getWorld("world").setTime(1000);

        pollForGameData();
        String jsonFromAwsSqs = "{\n" +
                "  \"blueTeam\": [\n" +
                "    \"vice9\"\n" +
                "  ],\n" +
                "  \"requiredPlayers\": 2,\n" +
                "  \"redTeam\": [\n" +
                "    \"NitroholicPls\"\n" +
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

    private void pollForGameData() {
        final String QUEUE_ENV_NAME = "SYNDICATE_MATCH_QUEUE_NAME";
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        final String QUEUE_NAME = System.getenv(QUEUE_ENV_NAME);
        if (QUEUE_NAME == null) {
            System.out.println("EXIT: " + QUEUE_ENV_NAME + " environment variable is null");
            System.exit(-1);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                final String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
                List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
                Message message = messages.get(0);
                System.out.println("found message:" + message.getBody());
                game = Game.juneauGameFactory(message.getBody());
                try {
                    game.addContainerMetaData();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("EXIT: Could not add container metadata.");
                    System.exit(-1);
                }
                sqs.deleteMessage(queueUrl, message.getReceiptHandle());
            }
        }.runTaskTimer(this, 0, 200);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (game.getState() != Game.GameState.DURING_GAME &&
                game.getState() != Game.GameState.CAGED
        ) {
            event.setCancelled(true);
        } else {
            return;
        }
//            Entity entityVictim = event.getEntity();
//            Entity entityHitter = event.getDamager();
//
//            if (entityHitter instanceof Player && entityVictim instanceof Player) {
//
//                Player hitter = (Player) entityHitter;
//                Player hit = (Player) entityVictim;
//
//                // CraftPlayer craft = (CraftPlayer) hit;
//                // float absFloat = craft.getHandle().getAbsorptionHearts();
//                // int absInt = (int) absFloat;
//                int absInt = 0;
//
//                String hitName = hit.getName();
//                int health = (int) (hit.getHealth() - event.getFinalDamage()) / 2;
//                double dmgD = event.getFinalDamage() / 2;
//                int dmg = (int) Math.round(dmgD);
//                int goneHealth = 10 - (health + dmg);
//
//                String damage = ActionBarHealth.formatDamage(dmg);
//                String hearts = ActionBarHealth.formatHealth(health);
//                String blackHearts = ActionBarHealth.formatBlackHearts(goneHealth);
//                String goldHearts = ActionBarHealth.formatGoldHearts(absInt);
//
//                if (health >= 0) {
//                    if (absInt > 0) {
//
//                        ActionBarHealth showHearts = new ActionBarHealth(MatchTeam.getChatColor(hit) + "" + hitName + " "
//                                + hearts + goldHearts);
//                        showHearts.sendToPlayer(hitter);
//
//                    } else {
//
//                        ActionBarHealth showHearts = new ActionBarHealth(MatchTeam.getChatColor(hit) + "" + hitName + " "
//                                + hearts + damage + blackHearts);
//                        showHearts.sendToPlayer(hitter);
//
//                    }
//                }
//            }
//        }
    }

    public void resetPlayerHealthAndInventory(Player player) {
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

        Team Red = board.registerNewTeam("Red");
        Red.setPrefix(ChatColor.RED + "");
        Red.setNameTagVisibility(NameTagVisibility.ALWAYS);

        Team Blue = board.registerNewTeam("Blue");
        Blue.setPrefix(ChatColor.BLUE + "");
        Blue.setNameTagVisibility(NameTagVisibility.ALWAYS);

        Team kill = board.registerNewTeam("kills");
        kill.setSuffix("0");
        kill.addEntry("Kills: §a");
        title.getScore("Kills: §a").setScore(8);

        Team timer = board.registerNewTeam("timer");
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

        Objective j = board.registerNewObjective("health2", "health");
        j.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    @EventHandler
    public static void onPlayerChat(AsyncPlayerChatEvent event) {
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

        String deathMessage = MatchTeam.getChatColor(player).toString() +
                killedString +
                ChatColor.GRAY + " was killed by " +
                MatchTeam.getChatColor(killer) + killerString +
                ChatColor.GRAY + ".";
        event.setDeathMessage(deathMessage);
        event.setDroppedExp(0);
        killer.playSound(killer.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);
    }

    private void sendDeadPlayerToSpawn(Player player) {
        resetPlayerHealthAndInventory(player);
        player.teleport(MatchTeam.getSpawnLocation(player));
        zeroPlayerVelocity(player);
        player.playSound(player.getLocation(), Sound.HURT_FLESH, 1.0f, 0.9f);
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (game.getState() == Game.GameState.CAGED) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (player.getItemInHand().getType() == Material.STAINED_CLAY) {
                    player.sendMessage(ChatColor.RED + "You can't place blocks there!");
                    player.playSound(player.getLocation(), Sound.DIG_STONE, 1.0f, 0.8f);
                }
            }
        }
    }

    @EventHandler
    public void cancelBlockPlacement(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        Block b = event.getBlock();
        int bX = b.getX();
        int bY = b.getY();
        int bZ = b.getZ();

        if (bX > 25 || bX < -25) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't place blocks there!");
            return;
        }
        if (bY > 99 || bY < 84) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't place blocks there!");
            return;
        }
        if (bZ > 20 || bZ < -20) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't place blocks there!");
            return;
        }
    }

    @EventHandler
    public void cancelBlockBreak(BlockBreakEvent event) {
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
        if (bY > 99 || bY < 84 || bX > 25 || bX < -25 || bZ > 20 || bZ < -20) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't break that block!");
            return;
        }
        if (bY == 99 && (bX == 25 || bX == -25) && (bZ == 4 || bZ == -4)) {
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

    public void toteScore(Player player, GoalLocationInfo goal) {
        GameScore score = GameScore.getInstance();
        cagePlayers();
        if (MatchTeam.getTeam(player) != goal.getTeam()) {
            score.increment(MatchTeam.getTeam(player));
            UUID scorerId = player.getUniqueId();
            game.addGoalInfo(scorerId);
            ChatBroadcasts.scoreMessage(game, player);
            spawnFireworks(player);
            // Scoreboard board = player.getScoreboard();
            // board.getTeam("goals").setSuffix("" + newGoals);
        }
    }

    private Location getOrigin() {
        return (new Location(Bukkit.getWorld("world"), 0, 0, 0, 0, 0));
    }

    private void buildCages() {
        EditSession editSession = new EditSession(new BukkitWorld(getOrigin().getWorld()), MAX_BLOCKS);
        editSession.enableQueue();
        for (TeamType team : MatchTeam.getTeams()) {
            Location cageLocation = MatchTeam.getCageLocation(team);
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
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1.0f, 1.0f);
                }
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
            sendTitles(player);
            resetPlayerHealthAndInventory(player);
            setGameModeForPlayer(player);
            player.teleport(MatchTeam.getCagePlayerLocation(player));
        }
    }

    public void checkForGoal(Player player) {
        if (game.getState() != Game.GameState.DURING_GAME) return;
        final List<GoalLocationInfo> goalList = new ArrayList<>();
        goalList.add(MatchTeam.getBlueGoalMeta());
        goalList.add(MatchTeam.getRedGoalMeta());

        for (GoalLocationInfo goal : goalList) {
            if (MatchTeam.getTeam(player) != goal.getTeam()) {
                if (goal.getBoundingBox().contains(
                        player.getLocation().getX(),
                        player.getLocation().getY(),
                        player.getLocation().getZ())) {
                    toteScore(player, goal);
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

                    Bukkit.broadcastMessage(MatchTeam.getChatColor(player) + killed + ChatColor.GRAY + " was hit into the void by "
                            + MatchTeam.getChatColor(damager) + killer + ChatColor.GRAY + ".");

                    damager.playSound(damager.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);

                }

            } else {
                if (game.getState() == Game.GameState.DURING_GAME) {
                    String killed = player.getName();
                    Bukkit.broadcastMessage(MatchTeam.getChatColor(player) + killed + ChatColor.GRAY + " fell into the void.");
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
        event.setJoinMessage("");
        Bukkit.broadcastMessage("Welcome to the server " + player.getName() + "!");
        setGameModeForPlayer(player);
        resetPlayerHealthAndInventory(player);
        final String UNINITIALIZED_MESSAGE = "Game not initialized";

        if (game == null) {
            System.err.println(UNINITIALIZED_MESSAGE);
            player.kickPlayer(UNINITIALIZED_MESSAGE);
            return;
        }
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
        switch (game.getState()) {
            case DURING_GAME:
                sendDeadPlayerToSpawn(player);
                break;
            case CAGED:
                player.teleport(MatchTeam.getCagePlayerLocation(player));
                break;
            default:
                // do nothing
        }
    }

    private void setInventoryForPlayer(Player player) {
        switch (game.getState()) {
            case BEFORE_GAME:
            case AFTER_GAME:
                player.getInventory().clear();
                break;
            case DURING_GAME:
            case CAGED:
                Inventory.setDefaultInventory(player);
                break;
            default:
                // do nothing
        }
    }

    private void setGameModeForPlayer(Player player) {
        switch (game.getState()) {
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

    private void buildScoreboards() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        for (Iterator<String> it = game.getJoinedPlayers(); it.hasNext(); ) {
            String playerName = it.next();
            createScoreboardForPlayer(manager, Bukkit.getPlayer(playerName));
        }
    }

    public void createScoreboardForPlayer(ScoreboardManager manager, Player player) {
        Scoreboard board = manager.getNewScoreboard();
        player.setScoreboard(board);

        Objective objective = board.registerNewObjective("title", "dummy");
        objective.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "BRIDGE");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team timer = board.registerNewTeam(String.valueOf(scoreboardSections.TIMER));
        timer.addEntry(TIMER_STRING);
        timer.setSuffix("15:00");
        timer.setPrefix("");
        objective.getScore(TIMER_STRING).setScore(0);

        GameScore.initialize(board, objective);

        // getScore() sets a line with that string. Go figure.
//        Score redGoals = objective.getScore(ChatColor.RED + "[R] " + ChatColor.GRAY + "⬤⬤⬤⬤⬤");
//        Score blueGoals = objective.getScore(ChatColor.BLUE + "[B] " + ChatColor.GRAY + "⬤⬤⬤⬤⬤");
//        blueGoals.setScore(0);
//        redGoals.setScore(0);

//        redgoals.setScore(10);

//        Team red = board.registerNewTeam(String.valueOf(scoreboardSections.RED));
//        red.setPrefix(ChatColor.RED + "");
//        Team blue = board.registerNewTeam(String.valueOf(scoreboardSections.BLUE));
//        blue.setPrefix(ChatColor.BLUE + "");
    }

    private void startGame() {
        cagePlayers();
        buildScoreboards();
        broadcastStartMessages();
        buildScoreboards();
        startClock();
    }

    private void startClock() {
        new BukkitRunnable() {
            @Override
            public void run() {
                String timeRemaining = game.getRemainingTime();
                for (Iterator<String> it = game.getJoinedPlayers(); it.hasNext(); ) {
                    String playerName = it.next();
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null) {
                        Scoreboard board = player.getScoreboard();
                        Team timer = board.getTeam(String.valueOf(scoreboardSections.TIMER));
                        timer.setSuffix(timeRemaining);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20);
    }

    private void broadcastStartMessages() {
        for (TeamType team : MatchTeam.getTeams()) {
            for (String playerName : MatchTeam.getPlayers(team)) {
                Player player = Bukkit.getPlayer(playerName);
                TeamType opposingTeam = MatchTeam.getOpposingTeam(team);
                String opponentNames = String.join(", ", MatchTeam.getPlayers(opposingTeam));
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
        MatchTeam.addToTeam(teamColor, player);
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
                    MatchTeam.addToTeam(TeamType.RED, player);
                    Scoreboard board = player.getScoreboard();
                    org.bukkit.scoreboard.Team Red = board.getTeam("Red");
                    Red.addEntry(player.getName());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            List<String> blueTeam = MatchTeam.getBlueTeam();
                            String blueTeamString = blueTeam.toString().replace("[", "").replace("]", "");
                            org.bukkit.scoreboard.Team Blue = board.getTeam("Blue");
                            Blue.addEntry(blueTeamString);
                        }
                    }.runTaskLater(this, 1);
                } else {
                    MatchTeam.addToTeam(TeamType.BLUE, player);
                    Scoreboard board = player.getScoreboard();
                    org.bukkit.scoreboard.Team Blue = board.getTeam("Blue");
                    Blue.addEntry(player.getName());
                    List<String> redTeam = MatchTeam.getRedTeam();
                    String redTeamString = redTeam.toString().replace("[", "").replace("]", "");
                    org.bukkit.scoreboard.Team Red = board.getTeam("Red");
                    Red.addEntry(redTeamString);
                }
                i++;

            }
            buildCages();
            cagePlayers();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String opponent;
                if (MatchTeam.getTeam(player) == TeamType.RED) {
                    List<String> blueTeam = MatchTeam.getBlueTeam();
                    opponent = blueTeam.toString().replace("[", "").replace("]", "");
                    ChatBroadcasts.gameStartMessage(player, opponent);
                } else {
                    List<String> redTeam = MatchTeam.getRedTeam();
                    opponent = redTeam.toString().replace("[", "").replace("]", "");
                    ChatBroadcasts.gameStartMessage(player, opponent);
                }

            }
        }

        if (label.equalsIgnoreCase("myteam")) {
            commandSender.sendMessage(MatchTeam.getTeamName((Player) commandSender));
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

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        e.setQuitMessage("");
    }

    public void onDisable() {
        MatchTeam.clearTeams();
    }

    @EventHandler
    public void entityDamageEvent(final EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void foodChangeEvent(final FoodLevelChangeEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            final Player player = (Player) event.getEntity();
            event.setCancelled(true);
            player.setFoodLevel(20);
        }
    }

    public void sendTitles(Player player) {
        String scorerName;

        new BukkitRunnable() {
            int secondsUntilCagesOpen = 5;

            public void run() {
                if (secondsUntilCagesOpen > 0) {
                    String titleText = (game.hasScore()) ? game.getMostRecentScorerName() + " scored!" : "";
                    Title title = new Title(titleText, "&7Cages open in: &a" + secondsUntilCagesOpen + "s&7...", 0, 3, 0);
                    title.send(player);
                    player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f);
                    secondsUntilCagesOpen--;
                } else {
                    Title title = new Title("", "&aFight!", 0, 1, 1);
                    title.send(player);
                    this.cancel();
                }
            }
        }.runTaskTimer(this, 0, 20);
    }

    private void spawnFireworks(Player player) {


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
        }.runTaskLater(this, 2);

    }

    @EventHandler
    public void onBowHit(EntityDamageByEntityEvent event) {

        Entity entityVictim = event.getEntity();
        Entity entityHitter = event.getDamager();

        if (entityHitter instanceof Arrow && entityVictim instanceof Player) {

            Arrow arrow = (Arrow) entityHitter;
            if (arrow.getShooter() instanceof Player) {
                Player shoota = (Player) arrow.getShooter();
                Player shot = (Player) entityVictim;

                double shotHealth = shot.getHealth();
                DecimalFormat format = new DecimalFormat("##.#");
                String heartValue = format.format(shotHealth);

                String shotName = shot.getName();
                shoota.sendMessage(ChatColor.GRAY + shotName + " is on " + ChatColor.RED + heartValue + ChatColor.GRAY + " HP!");
                shoota.playSound(shoota.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f);

//                // CraftPlayer craft = (CraftPlayer) shot;
//                // float absFloat = craft.getHandle().getAbsorptionHearts();
//                // int absInt = (int) absFloat;
//                int absInt = 0;
//
//                int health = (int)(shot.getHealth() - event.getFinalDamage())/2;
//                double dmgD = event.getFinalDamage()/2;
//                int dmg = (int) Math.round(dmgD);
//                int goneHealth = 10 - (health + dmg);
//
//                String damage = ActionBarHealth.formatDamage(dmg);
//                String hearts = ActionBarHealth.formatHealth(health);
//                String blackHearts = ActionBarHealth.formatBlackHearts(goneHealth);
//                String goldHearts = ActionBarHealth.formatGoldHearts(absInt);
//
//                if(health >= 0){
//                    if(absInt > 0){
//
//                        ActionBarHealth showHearts = new ActionBarHealth(MatchTeam.getChatColor(shot) + "" + shotName + " "
//                                + hearts + goldHearts);
//                        showHearts.sendToPlayer(shoota);
//
//                    }else{
//
//                        ActionBarHealth showHearts = new ActionBarHealth(MatchTeam.getChatColor(shot) + "" + shotName + " "
//                                + hearts + damage + blackHearts);
//                        showHearts.sendToPlayer(shoota);
//
//                    }
//                }
            }
        }
    }

    public static void main(String[] args) throws URISyntaxException, IOException, SerializeException {
//    public void printContainerMetaData() throws IOException, URISyntaxException {
//        String url = System.getenv("ECS_CONTAINER_METADATA_URI_V4");

        URI uri = new URI("https://www.google.com");
        String foo = RestClient.create().plainText().build().doGet(uri).getResponseAsString();
//        System.out.println(foo);

        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        String QUEUE_NAME = System.getenv("SYNDICATE_MATCH_QUEUE_NAME");
        System.out.println(QUEUE_NAME);
        String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
        System.out.println(queueUrl);
        List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
        System.out.println("Messages: " + messages.size());
        for (Message message : messages) {
            System.out.println("found message:" + message.getBody());
            Game myGame = Game.juneauGameFactory(message.getBody());
            JsonSerializer jsonSerializer = JsonSerializer.DEFAULT_READABLE;
            String serialzedNewGame = jsonSerializer.serialize(myGame);
            System.out.println(serialzedNewGame);
            sqs.deleteMessage(queueUrl, message.getReceiptHandle());
        }
    }
}



