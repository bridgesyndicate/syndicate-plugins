package gg.bridgesyndicate.bridgeteams;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

public final class BridgeTeams extends JavaPlugin implements Listener {

    private ProtocolManager protocolManager;
    private static final int MAX_BLOCKS = 10000;
    private static Game game = null;
    private final int NO_START_ABORT_TIME_IN_SECONDS = 120;

    public enum scoreboardSections {TIMER}

    private static final String TIMER_STRING = "Time Left: " + ChatColor.GREEN;
    private static final String WAS_KILLED_BY = " was killed by ";
    private static final String WAS_VOIDED_BY = " was hit into the void by ";

    private CuboidClipboard clipboard = null;


    private void printWorldRules() {
        for (String gameRule : Bukkit.getWorld("world").getGameRules()) {
            System.out.println(gameRule + " : " + Bukkit.getWorld("world").getGameRuleValue(gameRule));
        }
    }

    @Override
    public void onEnable() {
        System.out.println(this.getClass() + " is loading.");
        this.getServer().getPluginManager().registerEvents(this, this);
        protocolManager = ProtocolLibrary.getProtocolManager();
        Bukkit.getWorld("world").setGameRuleValue("keepInventory", "true");
        Bukkit.getWorld("world").setGameRuleValue("naturalRegeneration", "false");
        Bukkit.getWorld("world").setGameRuleValue("doDaylightCycle", "false");
        Bukkit.getWorld("world").setTime(1000);
        try {
            prepareCages();
        } catch (com.sk89q.worldedit.data.DataException | IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: Could not prepare cages. Exiting.");
            System.exit(-1);
        }
        pollForGameData();
    }

    private void prepareCages() throws com.sk89q.worldedit.data.DataException, IOException {
        final File schematic = new File("/app/minecraft-home/plugins/WorldEdit/schematics/mushroomcage.schematic");
        SchematicFormat schematicFormat = SchematicFormat.getFormat(schematic);
        clipboard = schematicFormat.load(schematic);
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
                if ( messages.size() > 0 ) {
                    Message message = messages.get(0);
                    System.out.println("found message on " + QUEUE_NAME + ": " + message.getBody());
                    game = Game.deserialize(message.getBody());
                    try {
                        game.addContainerMetaData();
                        HttpClient.put(game, HttpClient.PUT_REASONS.CONTAINER_METADATA);
                        abortGameOnTimeout();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("EXIT: Could not add container metadata.");
                        System.exit(-1);
                    }
                    sqs.deleteMessage(queueUrl, message.getReceiptHandle());
                    this.cancel();
                } else {
                    System.out.println("Polling for game data. No games. Trying again in 10s.");
                }
            }
        }.runTaskTimer(this, 0, 200);
    }

    private void abortGameOnTimeout() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (game.getState() == Game.GameState.BEFORE_GAME) {
                    Game abortedGame = game;
                    game = null;
                    System.out.println("Game has not started in " + NO_START_ABORT_TIME_IN_SECONDS
                    + " seconds. Aborting.");
                    abortedGame.setState(Game.GameState.ABORTED);
                    try {
                        HttpClient.put(abortedGame, HttpClient.PUT_REASONS.ABORTED_GAME);
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.kickPlayer("Game did not start within " + NO_START_ABORT_TIME_IN_SECONDS);
                    }
                    pollForGameData();
                }
            }
        }.runTaskLater(this, NO_START_ABORT_TIME_IN_SECONDS * 20);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (game.getState() != Game.GameState.DURING_GAME &&
                game.getState() != Game.GameState.CAGED
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player
        && MatchTeam.getTeam((Player) event.getDamager()) != MatchTeam.getTeam((Player) event.getEntity())
        ) {
            computeKnockback(event);
        }
    }


    private void computeKnockback (EntityDamageByEntityEvent event) {
//        Vector damagedPlayersVelocity = event.getEntity().getVelocity();
//        Vector knockBackVector = damagedPlayersVelocity.multiply(-1);
        Player player = (Player) event.getEntity();
        Vector v = player.getVelocity();
        v.setX(0);
        v.setY(0);
        v.setZ(0);
        player.setVelocity(v);
        PacketContainer explosion = protocolManager.createPacket(PacketType.Play.Server.EXPLOSION);
        System.out.println("computeKnockback");
        explosion.getDoubles().
                write(0, player.getLocation().getX()).
                write(1, player.getLocation().getY()).
                write(2, player.getLocation().getZ());
        explosion.getFloat().write(0, 0.6F);
        explosion.getFloat().write(1, 0.4F);
        explosion.getFloat().write(2, 0.6F);
        try {
            protocolManager.sendServerPacket(player, explosion);
            System.out.println("explosion sent");
        } catch (InvocationTargetException e) {
            throw new RuntimeException(
                    "Cannot send packet " + explosion, e);
        }
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

    @EventHandler
    public static void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage();
        event.setCancelled(true);
        Bukkit.broadcastMessage(ChatColor.GREEN + "[GAME] " + player.getName() + ChatColor.WHITE + ": " + msg);
    }

    @EventHandler
    public void onDeathOfPlayer(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        onDeathOfPlayerImpl(player, killer, event);
    }

    private void onDeathOfPlayerImpl(Player player, Player killer, Event event){
        sendDeadPlayerToSpawn(player);
        player.setNoDamageTicks(50);
        if(game.getState() == Game.GameState.DURING_GAME) {
            game.addKillInfo(killer.getUniqueId());
            GameScore score = GameScore.getInstance();
            score.updateKillersKills(killer, game);
            sendDeathMessages(player, killer, event);
        }
    }

    private void sendDeathMessages(Player player, Player killer, Event event) {
        String deathMessage = MatchTeam.getChatColor(player).toString()
                + player.getName() + ChatColor.GRAY;
        deathMessage = deathMessage.concat(((event instanceof PlayerDeathEvent) ?
                WAS_KILLED_BY : WAS_VOIDED_BY ));
        deathMessage = deathMessage.concat(MatchTeam.getChatColor(killer) + killer.getName());
        deathMessage = deathMessage.concat(ChatColor.GRAY + ".");

        if (event instanceof PlayerDeathEvent) {
            PlayerDeathEvent playerDeathEvent = (PlayerDeathEvent) event;
            playerDeathEvent.setDeathMessage(deathMessage);
            playerDeathEvent.setDroppedExp(0);
        } else {
            Bukkit.broadcastMessage(deathMessage);
        }
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
        final String NO_BLOCKS_THERE = ChatColor.RED + "You can't place blocks there!";

        Block b = event.getBlock();
        int bX = b.getX();
        int bY = b.getY();
        int bZ = b.getZ();

        if (bX > 25 || bX < -25) {
            event.setCancelled(true);
            player.sendMessage(NO_BLOCKS_THERE);
            return;
        }
        if (bY > 99 || bY < 84) {
            event.setCancelled(true);
            player.sendMessage(NO_BLOCKS_THERE);
            return;
        }
        if (bZ > 20 || bZ < -20) {
            event.setCancelled(true);
            player.sendMessage(NO_BLOCKS_THERE);
        }
    }

    @EventHandler
    public void cancelBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        final String NO_BREAK_THERE = ChatColor.RED + "You can't break that block!";
        Block b = event.getBlock();
        int bX = b.getX();
        int bY = b.getY();
        int bZ = b.getZ();

        if (event.getBlock().getType() != Material.STAINED_CLAY) {
            event.setCancelled(true);
            player.sendMessage(NO_BREAK_THERE);
            return;
        }
        if (bY > 99 || bY < 84 || bX > 25 || bX < -25 || bZ > 20 || bZ < -20) {
            event.setCancelled(true);
            player.sendMessage(NO_BREAK_THERE);
            return;
        }
        if (bY == 99 && (bX == 25 || bX == -25) && (bZ == 4 || bZ == -4)) {
            event.setCancelled(true);
            player.sendMessage(NO_BREAK_THERE);
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

    public void toteScore(Player player, GoalLocationInfo goal) throws JsonProcessingException {
        GameScore score = GameScore.getInstance();
        score.increment(MatchTeam.getTeam(player));
        game.addGoalInfo(player.getUniqueId());
        score.updatePlayersGoals(player, game);
        if (!game.over()) {
            cagePlayers();
            BridgeFireworks fireworks = new BridgeFireworks(this);
            fireworks.spawnFireworks(player);
        } else {
            endGame();
        }
        ChatBroadcasts.scoreMessage(game, player);
    }

    private Location getOrigin() {
        return (new Location(Bukkit.getWorld("world"), 0, 0, 0, 0, 0));
    }

    private void buildCages() {
        //EditSession editSession = new EditSession(new BukkitWorld(getOrigin().getWorld()), MAX_BLOCKS);
        //editSession.enableQueue();
        for (TeamType team : MatchTeam.getTeams()) {
            Location cageLocation = MatchTeam.getCageLocation(team);
//            try {
//                clipboard.paste(editSession, BukkitUtil.toVector(cageLocation), true);
//            } catch (MaxChangedBlocksException e) {
//                e.printStackTrace();
//                System.out.println("ERROR: Could not build cages. Exiting.");
//                System.exit(-1);
//            }
        }
       // editSession.flushQueue();

        new BukkitRunnable() {
            @Override
            public void run() {
                game.setState(Game.GameState.DURING_GAME);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1.0f, 1.0f);
                }
                //editSession.undo(editSession);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    setGameModeForPlayer(player);
                }
            }
        }.runTaskLater(this, 100);
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

    public void checkForGoal(Player player) throws JsonProcessingException {
        if (game.getState() != Game.GameState.DURING_GAME) return;

        for (GoalLocationInfo goal : BridgeGoals.getGoalList()) {
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
    public void playerMove(PlayerMoveEvent event) throws JsonProcessingException {
        final Player player = event.getPlayer();
        if (player.getLocation().getY() < 83) {
            if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                Entity entity = ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();
                if (entity instanceof Player) {
                    final Player killer = (Player) entity;
                    onDeathOfPlayerImpl(player, killer, event);
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
        final String UNINITIALIZED_MESSAGE = ChatColor.RED + "Game not initialized";
        Player player = event.getPlayer();
        event.setJoinMessage("");

        if (game == null) {
            System.err.println(UNINITIALIZED_MESSAGE);
            player.kickPlayer(UNINITIALIZED_MESSAGE);
            return;
        }

        setGameModeForPlayer(player);
        resetPlayerHealthAndInventory(player);

        // set waiting board
        if (game.hasPlayer(player)) {
            assignToTeam(player);
            System.out.println("joined players: " + game.getNumberOfJoinedPlayers() + "required players:" + game.getRequiredPlayers());
            Bukkit.broadcastMessage(ChatColor.GRAY + "Welcome " + MatchTeam.getChatColor(player) + player.getName() + ChatColor.GRAY + "! " + MatchTeam.getChatColor(player) + "[" + ChatColor.GRAY + game.getNumberOfJoinedPlayers() + "/" + game.getRequiredPlayers() + MatchTeam.getChatColor(player) + "]");
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
                player.getInventory().setHelmet(null);
                player.getInventory().setChestplate(null);
                player.getInventory().setLeggings(null);
                player.getInventory().setBoots(null);
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
            case AFTER_GAME:
                player.setGameMode(GameMode.ADVENTURE);
                break;
            case DURING_GAME:
                player.setGameMode(GameMode.SURVIVAL);
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
        objective.getScore(TIMER_STRING).setScore(7);

        GameScore.initialize(board, objective, game, player);
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
                if ( game.getState() == Game.GameState.AFTER_GAME) {
                    System.out.println("cancel the startclock timer");
                    this.cancel();
                    return;
                }
                if (game.getRemainingTimeInSeconds() < 0 ) {
                    this.cancel();
                    try {
                        endGame();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                } else {
                    GameScore score = GameScore.getInstance();
                    score.updateGameClock(game);
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
                ChatBroadcasts.gameStartMessage(player, opponentNames, game);
            }
        }
    }

    //set holograms at each goal location

    private void makeSpectator(Player player) {
    }

    private void assignToTeam(Player player) {
        TeamType teamColor = game.getTeam(player);
        MatchTeam.addToTeam(teamColor, player);
        game.playerJoined(player.getName());
    }

    private void endGame() throws JsonProcessingException {
        game.setState(Game.GameState.AFTER_GAME);
        game.setEndTime();
        broadcastEndMessages();
        List<String> titles = BridgeTitles.getFinalTitles();
        for (Iterator<String> it = game.getJoinedPlayers(); it.hasNext(); ) {
            String playerName = it.next();
            Player player = Bukkit.getPlayer(playerName);
            setGameModeForPlayer(player);
            sendDeadPlayerToSpawn(player);
            BridgeFireworks fireworks = new BridgeFireworks(this);
            fireworks.spawnFireworks(player);
            player.getInventory().clear();
            Title title = new Title(titles.get(0), titles.get(1), 0, 6, 1);
            title.send(player);
        }
        new BukkitRunnable() {
            public void run() {
                if (game.getState() == Game.GameState.TERMINATE){
                    // everything is good. Quit
                } else {
                    System.out.println("ERROR: could not send end-of-game data");
                    try {
                        System.out.println(Game.serialize(game));
                    } catch (JsonProcessingException e) {
                        System.out.println("ERROR: Yikes, I could not send the end-of-game data and I could not serialize the game. This game is gone forever.");
                        e.printStackTrace();
                    }
                }
                System.exit(0);
            }
        }.runTaskLater(this, 600);

        new BukkitRunnable() {
            int attempt = 0;
            public void run() {
                if (attempt > 5) {
                    Exception e = new Exception("FAILED TO SEND GAME DATA");
                    e.printStackTrace();
                    this.cancel();
                }
                try {
                    HttpClient.put(game, HttpClient.PUT_REASONS.FINISHED_GAME);
                    game.setState(Game.GameState.TERMINATE);
                    this.cancel();
                } catch (URISyntaxException | IOException e) {
                    e.printStackTrace();
                    attempt++;
                }
            }
        }.runTaskTimer(this, 0, 40);
    }

    private void broadcastEndMessages() {
        for (TeamType team : MatchTeam.getTeams()) {
            for (String playerName : MatchTeam.getPlayers(team)) {
                Player player = Bukkit.getPlayer(playerName);
                TeamType opposingTeam = MatchTeam.getOpposingTeam(team);
                String opponentNames = String.join(", ", MatchTeam.getPlayers(opposingTeam));
                ChatBroadcasts.gameEndMessage(player, opponentNames, game);
            }
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
        new BukkitRunnable() {
            int secondsUntilCagesOpen = 5;

            public void run() {
                if (secondsUntilCagesOpen > 0) {
                    String titleText = "";
                    if (game.hasScore()){
                        String scorerName = game.getMostRecentScorerName();
                        Player scorer = Bukkit.getPlayer(scorerName);
                        ChatColor chatColor = MatchTeam.getChatColor(scorer);
                        titleText = chatColor + scorerName + " scored!";
                    }
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

    @EventHandler
    public void onBowHit(EntityDamageByEntityEvent event) {

        Entity entityVictim = event.getEntity();
        Entity entityHitter = event.getDamager();

        if (entityHitter instanceof Arrow && entityVictim instanceof Player) {

            Arrow arrow = (Arrow) entityHitter;
            if (arrow.getShooter() instanceof Player) {
                Player shoota = (Player) arrow.getShooter();
                Player shot = (Player) entityVictim;

                if(!(MatchTeam.getTeam(shoota).equals(MatchTeam.getTeam(shot)))) {
                    double shotHealth = shot.getHealth();
                    DecimalFormat format = new DecimalFormat("##.#");
                    String heartValue = format.format(shotHealth);
                    double damage = event.getFinalDamage();
                    double absorptionDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.ABSORPTION);
                    double futureHealth = shot.getHealth() - damage - absorptionDamage;
                    double formattedFutureHealth = Math.ceil(futureHealth * 10)/10;

                    System.out.println("finalsdamage: " + event.getFinalDamage());
                    System.out.println("getdamage: " + event.getDamage());
                    System.out.println("getoriginaldamage: " + event.getOriginalDamage(EntityDamageEvent.DamageModifier.ABSORPTION));
                    System.out.println(heartValue);

                    String shotName = shot.getName();
                    if (formattedFutureHealth > 0)
                        shoota.sendMessage(ChatColor.GRAY + shotName + " is on " + ChatColor.RED + formattedFutureHealth + ChatColor.GRAY + " HP!");
                    shoota.playSound(shoota.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f);
                }
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

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
//    public void printContainerMetaData() throws IOException, URISyntaxException {
//        String url = System.getenv("ECS_CONTAINER_METADATA_URI_V4");
//        JsonSerializer jsonSerializer = JsonSerializer.DEFAULT_READABLE;
//        String serialzedNewGame = jsonSerializer.serialize(myGame);
//        System.out.println(serialzedNewGame);

//        URI uri = new URI("https://www.google.com");
//        String foo = RestClient.create().plainText().build().doGet(uri).getResponseAsString();
//        System.out.println(foo);
//
//        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
//        String QUEUE_NAME = System.getenv("SYNDICATE_MATCH_QUEUE_NAME");
//        System.out.println(QUEUE_NAME);
//        String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
//        System.out.println(queueUrl);
//        List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
//        System.out.println("Messages: " + messages.size());
//        for (Message message : messages) {
//            System.out.println("found message:" + message.getBody());
//            Game myGame = Game.juneauGameFactory(message.getBody());
//            JsonSerializer jsonSerializer = JsonSerializer.DEFAULT_READABLE;
//            String serialzedNewGame = jsonSerializer.serialize(myGame);
//            System.out.println(serialzedNewGame);
//            sqs.deleteMessage(queueUrl, message.getReceiptHandle());
//        }
    }
}



