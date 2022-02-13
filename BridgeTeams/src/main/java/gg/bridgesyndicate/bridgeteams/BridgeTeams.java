package gg.bridgesyndicate.bridgeteams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import gg.bridgesyndicate.util.BoundingBox;
import gg.bridgesyndicate.util.ReadFile;
import gg.bridgesyndicate.util.Seconds;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
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
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public final class BridgeTeams extends JavaPlugin implements Listener {
    private static Game game = null;
    private static Inventory inventory = null;
    private static ArrowHandler arrowHandler = null;
    public enum scoreboardSections {TIMER}
    private static final String TIMER_STRING = "Time Left: " + ChatColor.GREEN;
    private static final String WAS_KILLED_BY = " was killed by ";
    private static final String WAS_VOIDED_BY = " was thrown off a cliff by ";
    private static final String WAS_SHOT_BY = " was shot by ";
    List<BridgeSchematicBlock> bridgeSchematicBlockList = null;
    private int[] cageSchematicIntegerList = null;
    private int cageSchematicIntegerListSize = 0;
    public static HashMap<UUID, Long> lastHitTimestampInMillis = new HashMap<>();
    public static HashMap<UUID, Scoreboard> disconnectedPlayerScoreboard = new HashMap<>();

    public BridgeTeams() {
        super();
    }

    protected BridgeTeams(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    public Inventory getInventory() { return inventory; }
    public Game getGame() { return game; }
    public static int getRequiredPlayers() { return game.getRequiredPlayers(); }

    @Override
    public void onEnable() {
        System.out.println(this.getClass() + " is loading.");
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(new ChatHandler(),this);
        this.getServer().getPluginManager().registerEvents(new ClickHandler(),this);
        this.getServer().getPluginManager().registerEvents(new ArrowHandler(this),this);
        setGameRules();
        MapMetadata mapMetadata = prepareMapMetadata();
        MatchTeam.setMapMetadata(mapMetadata);
        inventory = new Inventory(SyndicateEnvironment.SYNDICATE_ENV() != Environments.TEST);
        arrowHandler = new ArrowHandler(this);
        prepareCages();
        GameDataPoller gameDataPoller = GameDataPollerFactory.produce();
        gameDataPoller.poll(this);
    }

    private MapMetadata prepareMapMetadata() {
        String mapMetaDataJson = loadMapMetaDataJson();
        return MapMetadata.deserialize(mapMetaDataJson);
    }

    private void setGameRules() {
        Bukkit.getWorld("world").setGameRuleValue("keepInventory", "true");
        Bukkit.getWorld("world").setGameRuleValue("naturalRegeneration", "false");
        Bukkit.getWorld("world").setGameRuleValue("doDaylightCycle", "false");
        Bukkit.getWorld("world").setGameRuleValue("randomTickSpeed", "0");
    }

    private String loadMapMetaDataJson() {
        try {
            return(ReadFile.read(ReadFile.pathToResources() + "meta.json"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void prepareCages() {
        try {
            final String schematicJson = ReadFile.read(ReadFile.pathToResources() + "cage.json");
            ObjectMapper objectMapper = new ObjectMapper();
            CollectionType typeReference =
                    TypeFactory.defaultInstance().constructCollectionType(List.class, BridgeSchematicBlock.class);
            bridgeSchematicBlockList = objectMapper.readValue(schematicJson, typeReference);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: Could not prepare cages. Exiting.");
            System.exit(-1);
        }
        cageSchematicIntegerListSize = bridgeSchematicBlockList.size();
        cageSchematicIntegerList = new int[cageSchematicIntegerListSize * 5];
        for (int i = 0; i < cageSchematicIntegerListSize ; i++) {
            BridgeSchematicBlock bridgeSchematicBlock = bridgeSchematicBlockList.get(i);
            cageSchematicIntegerList[i * 5]     = bridgeSchematicBlock.x;
            cageSchematicIntegerList[i * 5 + 1] = bridgeSchematicBlock.y;
            cageSchematicIntegerList[i * 5 + 2] = bridgeSchematicBlock.z;
            cageSchematicIntegerList[i * 5 + 3] = bridgeSchematicBlock.id;
            cageSchematicIntegerList[i * 5 + 4] = bridgeSchematicBlock.data;
        }
    }

    public void receiveGame(Game game) {
        BridgeTeams.game = game;
        String mapName = System.getProperty("mapName", "errorMapNotSet");
        System.out.println("using map " + mapName);
        BridgeTeams.game.setMapName(mapName);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if ( (game.getState() != Game.GameState.DURING_GAME &&
                game.getState() != Game.GameState.CAGED)
                ||
                (event.getEntity() instanceof Player && event.getDamager() instanceof Player
                        && MatchTeam.getTeam((Player) event.getDamager()) == MatchTeam.getTeam((Player) event.getEntity())
                 )
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player
        && MatchTeam.getTeam((Player) event.getDamager()) != MatchTeam.getTeam((Player) event.getEntity())
        ) {
            Player damage_maker = (Player) event.getDamager();
            UUID id = damage_maker.getUniqueId();
            lastHitTimestampInMillis.put(id, System.currentTimeMillis());
        }
    }

    public void resetPlayerHealthAndInventory(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExp(0);
        player.setLevel(0);
        player.setNoDamageTicks(50);
        setInventoryForPlayer(player);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        zeroPlayerVelocity(player);
        arrowHandler.cancelArrowCooldown(player);
    }

    @EventHandler
    public void onDeathOfPlayer(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        onDeathOfPlayerImpl(player, killer, event);
    }

    private void onDeathOfPlayerImpl(Player player, Player killer, Event event){
        sendDeadPlayerToSpawn(player);
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

        if (event instanceof PlayerDeathEvent) {
            Entity entity = ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();
            if (entity instanceof Arrow) {
                deathMessage = deathMessage.concat(WAS_SHOT_BY);
            } else {
                deathMessage = deathMessage.concat(WAS_KILLED_BY);
            }
        } else {
            deathMessage = deathMessage.concat(WAS_VOIDED_BY);
        }

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
    public void blockPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();


        final String NO_BLOCKS_THERE = ChatColor.RED + "You can't place blocks there!";
        Block b = event.getBlock();
        double bX = b.getX();
        double bY = b.getY();
        double bZ = b.getZ();

        // i'm sure there's a better way to do this but for now i'm going to do it like this
        BoundingBox allowedBlocks = MatchTeam.getBuildLimits();

        if (bY > allowedBlocks.getMaxY() || bY < allowedBlocks.getMinY() || bX > allowedBlocks.getMaxX() || bX < allowedBlocks.getMinX() || bZ > allowedBlocks.getMaxZ() || bZ < allowedBlocks.getMinZ()) {
            event.setCancelled(true);
            player.sendMessage(NO_BLOCKS_THERE);
        } else {
            b.setMetadata("placedByPlayer", new FixedMetadataValue(this, "Metadata value")); // this tells the server that this block was placed by a player, and not already there
        }


    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();


        final String NO_BREAK_THERE = ChatColor.RED + "You can't break that block!";
        Block b = event.getBlock();
        double bX = b.getX();
        double bY = b.getY();
        double bZ = b.getZ();

        if ((event.getBlock().getType() != Material.STAINED_CLAY) // the only blocks you are ever allowed to break is stained clay
                || ((!b.hasMetadata("placedByPlayer")) && // checks to see if the block was not placed by a player
                (!(bZ == 0 && (bX < 21 && bX > -21) && (bY < 93 && bY > 83))))) { // checks to see if it's not part of the bridge. Also, these values are ALWAYS the same

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

    public void toteScore(Player player) throws JsonProcessingException {
        GameScore score = GameScore.getInstance();
        score.increment(MatchTeam.getTeam(player));
        game.addGoalInfo(player.getUniqueId());
        score.updatePlayersGoals(player, game);
        ChatBroadcasts.scoreMessage(game, player);
        String playerName = MatchTeam.getChatColor(player) + player.getName();
        game.setLastScorerName(playerName);
        if (!game.over()) {
            cagePlayers();
            BridgeFireworks fireworks = new BridgeFireworks(this);
            fireworks.spawnFireworks(player);
        } else {
            endGame();
        }
    }

    private void buildOrDestroyCageAtLocation(BlockVector cageLocation, String createOrDestroy, TeamType team) {
        World world = Bukkit.getWorld("world");
        int cageX = cageLocation.getBlockX();
        int cageY = cageLocation.getBlockY();
        int cageZ = cageLocation.getBlockZ();
        for (int i = 0; i < cageSchematicIntegerListSize ; i++) {
            int id = (createOrDestroy.equals("create")) ? cageSchematicIntegerList[i * 5 + 3] : 0;
            byte data = (createOrDestroy.equals("create")) ? (byte) cageSchematicIntegerList[i * 5 + 4] : 0;
            if (team == TeamType.BLUE) { // turn the cage blue
                if (id == 100 && data == 0) {
                    id = 99;
                    data = 0;
                }
                if ( (id == 159 && (int) data == 14) || (id == 35 && (int) data == 14) ){
                    data = 11;
                }
            }
            setBlockInNativeWorld(world,
                    cageX + cageSchematicIntegerList[i * 5],
                    cageY + cageSchematicIntegerList[i * 5 + 1],
                    cageZ + cageSchematicIntegerList[i * 5 + 2],
                    id,
                    data
            );
        }
    }

    private void setBlockInNativeWorld(World world, int x, int y, int z, int blockId, byte data){
        net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) world).getHandle();
        BlockPosition bp = new BlockPosition(x, y, z);
        IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(blockId + (data << 12));
        nmsWorld.setTypeAndData(bp, ibd, 2);
    }

    private void buildCages(String createOrDestroy) {
        for (TeamType team : MatchTeam.getTeams()) {
            BlockVector cageLocation = MatchTeam.getCageLocation(team);
            buildOrDestroyCageAtLocation(cageLocation, createOrDestroy, team);
        }
        if (createOrDestroy.equals("destroy"))
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                game.setState(Game.GameState.DURING_GAME);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1.0f, 1.0f);
                }
                buildCages("destroy");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if(!player.getGameMode().equals(GameMode.SPECTATOR)) {
                        setGameModeForPlayer(player);
                    }
                }
            }
        }.runTaskLater(this, Seconds.toTicks(5.0f));
    }

    private void cagePlayers() {
        game.setState(Game.GameState.CAGED);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(!player.getGameMode().equals(GameMode.SPECTATOR)){
                player.teleport(MatchTeam.getCagePlayerLocation(player));
                cagedPlayerCountdownTitles(player);
                resetPlayerHealthAndInventory(player);
                setGameModeForPlayer(player);
            }
        }
        buildCages("create");
    }

    public void checkForGoal(Player player) throws JsonProcessingException {
        if (game.getState() != Game.GameState.DURING_GAME) return;

        for (GoalLocationInfo goal : BridgeGoals.getGoalList()) {
            if (MatchTeam.getTeam(player) != goal.getTeam()) {
                if (goal.getBoundingBox().contains(
                        player.getLocation().getX(),
                        player.getLocation().getY(),
                        player.getLocation().getZ())) {
                    toteScore(player);
                }
            }
        }
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent event) throws JsonProcessingException {

        final Player player = event.getPlayer();
        if(player.getGameMode().equals(GameMode.SPECTATOR)){
            return;
        }
        if (player.getLocation().getY() < 83) {
            if ((player.getLastDamageCause() instanceof EntityDamageByEntityEvent)
                    && (game.getState() == Game.GameState.DURING_GAME)) {

                long now = System.currentTimeMillis();
                Entity entity = ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();
                Player killer = (entity instanceof Arrow) ? (Player) (((Arrow) entity).getShooter()) : (Player) entity;

                UUID id = killer.getUniqueId();
                long timeLength = now - lastHitTimestampInMillis.get(id);
                int MAX_TIME_FOR_KILL_ATTRIBUTION = 4001;
                if (timeLength < MAX_TIME_FOR_KILL_ATTRIBUTION) {
                    onDeathOfPlayerImpl(player, killer, event);
                } else {
                    String shooter_killed = player.getName();
                    Bukkit.broadcastMessage(MatchTeam.getChatColor(player) + shooter_killed + ChatColor.GRAY + " fell into the void.");
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
        UUID id = player.getUniqueId();
        if (game == null) {
            final String UNINITIALIZED_MESSAGE = ChatColor.RED + "Game not initialized";
            System.err.println(UNINITIALIZED_MESSAGE);
            player.kickPlayer(UNINITIALIZED_MESSAGE);
            return;
        }

        setGameModeForPlayer(player);
        sendDeadPlayerToSpawn(player);
        lastHitTimestampInMillis.put(id, 0L);
        arrowHandler.cancelArrowCooldown(player);
        event.setJoinMessage("");

        if (game.hasPlayer(player)) {
            System.out.println("game has player " + player.getName());
            if (!game.hasJoinedPlayer(player)) {
                System.out.println("player " + player.getName() + " has not previously joined.");
                assignToTeam(player);
                System.out.println("joined players: " + game.getNumberOfJoinedPlayers() + ", required players: " + game.getRequiredPlayers());
                Bukkit.broadcastMessage(ChatColor.GRAY + "Welcome " + MatchTeam.getChatColor(player) +
                        player.getName() + ChatColor.GRAY + "! " + MatchTeam.getChatColor(player) +
                        "[" + ChatColor.GRAY + game.getNumberOfJoinedPlayers() + "/" + game.getRequiredPlayers() +
                        MatchTeam.getChatColor(player) + "]");
                inventory.addPlayer(player);
                if (game.getNumberOfJoinedPlayers() == game.getRequiredPlayers())
                    startGame();
            } else {
                System.out.println("player " + player.getName() + " already joined");
                Scoreboard board = disconnectedPlayerScoreboard.get(player.getUniqueId());
                player.setScoreboard(board);
                if(game.getState() != Game.GameState.BEFORE_GAME)
                    GameScore.updateScoreBubbles(board);
                teleportRejoinedPlayer(player);
                ChatBroadcasts.playerRejoinMessage(player);
            }
        } else {
            System.out.println(player.getName() + " is spectator.");
            makeSpectator(player);
            ChatBroadcasts.spectatorJoinMessage(player);
        }
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
                inventory.setDefaultInventory(player);
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
        startClock();
        playQueueAlert();
    }

    private void playQueueAlert() {
       for (TeamType team : MatchTeam.getTeams()) {
            for (String playerName : MatchTeam.getPlayers(team)) {
                Player player = Bukkit.getPlayer(playerName);
                player.playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 2.0f, 1.0f);
            }
        }
    }

    private void startClock() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if ( game.getState() == Game.GameState.AFTER_GAME ||
                        game.getState() == Game.GameState.TERMINATE) {
                    System.out.println("cancel the start clock timer");
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
        }.runTaskTimer(this, 0, Seconds.toTicks(1.0f));
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

    private void makeSpectator(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        createScoreboardForPlayer(manager, player);
        Scoreboard board = player.getScoreboard();
        GameScore.updateScoreBubbles(board);
        player.sendMessage(ChatColor.GRAY + "You have joined as a spectator");
        inventory.fullyClearInventory(player);
    }

    private void assignToTeam(Player player) {
        TeamType teamColor = game.getTeam(player);
        MatchTeam.addToTeam(teamColor, player);
        game.playerJoined(player.getName(), player.getUniqueId());
    }

    private void endGame() throws JsonProcessingException {
        game.setState(Game.GameState.AFTER_GAME);
        game.setEndTime();
        List<String> titles = BridgeTitles.getFinalTitles();
        for (Player player : Bukkit.getOnlinePlayers()) {
            ChatBroadcasts.gameEndMessage(player, game);
            setGameModeForPlayer(player);
            sendDeadPlayerToSpawn(player);
            BridgeFireworks fireworks = new BridgeFireworks(this);
            fireworks.spawnFireworks(player);
            player.getInventory().clear();
            Title title = new Title(titles.get(0), titles.get(1), 0, 6, 1);
            title.send(player);
        }
        new BukkitRunnable() { // terminate the container so a new, fresh, game container can launch
            public void run() {
                if (game.getState() != Game.GameState.TERMINATE) {
                    System.out.println("ERROR: could not send end-of-game data");
                    try {
                        System.out.println("GAME JSON");
                        System.out.println(Game.serialize(game));
                    } catch (JsonProcessingException e) {
                        System.out.println("ERROR: Yikes, I could not send the end-of-game data and I could not serialize the game. This game is gone forever.");
                        e.printStackTrace();
                    }
                }
                System.exit(0);
            }
        }.runTaskLater(this, Seconds.toTicks(15.0f));

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
        }.runTaskTimer(this, 0, Seconds.toTicks(2.0f)); // try to send the game data every 2 seconds
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        Player player = e.getPlayer();
        e.setQuitMessage("");
        Scoreboard board = player.getScoreboard();
        disconnectedPlayerScoreboard.put(player.getUniqueId(), board);
        if (game != null){
            if(player.getGameMode().equals(GameMode.SPECTATOR)){
                ChatBroadcasts.spectatorQuitMessage(player);
            } else {
                ChatBroadcasts.playerQuitMessage(player);
            }
        }
    }

    public void onDisable() {
        MatchTeam.clearTeams();
    }

    @EventHandler
    public void entityDamageEvent(final EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            if ((event.getCause() == EntityDamageEvent.DamageCause.FALL)
                || (event.getCause() == EntityDamageEvent.DamageCause.VOID)){
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

    @EventHandler
    public void onWeatherChange(final WeatherChangeEvent e) {
        e.setCancelled(true);
    }

    public void cagedPlayerCountdownTitles(Player player) {
        new BukkitRunnable() {
            int secondsUntilCagesOpen = 5;

            public void run() {
                if (secondsUntilCagesOpen > 0) {
                    String titleText = "";
                    if (game.hasScore()){
                        String scorer = game.getLastScorerName();
                        titleText = scorer + " scored!";
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
        }.runTaskTimer(this, 0, Seconds.toTicks(1.0f));
    }

    @EventHandler
    public void onBowHit(EntityDamageByEntityEvent event) {

        Entity entityVictim = event.getEntity();
        Entity entityHitter = event.getDamager();

        if (entityHitter instanceof Arrow && entityVictim instanceof Player) {

            Arrow arrow = (Arrow) entityHitter;
            if (arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();
                Player shot = (Player) entityVictim;

                UUID id = shooter.getUniqueId();
                lastHitTimestampInMillis.put(id, System.currentTimeMillis());

                if (MatchTeam.getTeam(shooter).equals(MatchTeam.getTeam(shot))) {
                    event.setCancelled(true);
                } else {
                    double damage = event.getFinalDamage();
                    double absorptionDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.ABSORPTION);
                    double futureHealth = shot.getHealth() - damage - absorptionDamage;
                    double formattedFutureHealth = Math.ceil(futureHealth * 10)/10;

                    String shotName = shot.getName();

                    if (formattedFutureHealth > 0)
                        shooter.sendMessage(ChatColor.GRAY + shotName + " is on " + ChatColor.RED + formattedFutureHealth + ChatColor.GRAY + " HP!");
                }

            }
        }
    }
}

class BridgeSchematicBlock {
    public int x;
    public int y;
    public int z;
    public int id;
    public int data;

    @SuppressWarnings("unused") //used via jackson deserialization
    public BridgeSchematicBlock() { }

    @SuppressWarnings("unused") //used via jackson deserialization
    public BridgeSchematicBlock(int x, int y, int z, int id, int data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.data = data;
    }
}

