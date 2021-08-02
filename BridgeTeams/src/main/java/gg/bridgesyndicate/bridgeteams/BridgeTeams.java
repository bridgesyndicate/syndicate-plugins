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
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.serializer.SerializeException;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.*;

public final class BridgeTeams extends JavaPlugin implements Listener {

    private static final int MAX_BLOCKS = 10000;
    private static Game game = null;

    public enum scoreboardSections {TIMER}

    private static final String TIMER_STRING = "Time Left: " + ChatColor.GREEN;
    private static final String WAS_KILLED_BY = " was killed by ";
    private static final String WAS_VOIDED_BY = " was hit into the void by ";

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
                    game = Game.juneauGameFactory(message.getBody());
                    try {
                        game.addContainerMetaData();
                        // post game to syndicate-web-service
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

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (game.getState() != Game.GameState.DURING_GAME &&
                game.getState() != Game.GameState.CAGED
        ) {
            event.setCancelled(true);
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
        game.addKillInfo(killer.getUniqueId());
        GameScore score = GameScore.getInstance();
        score.updateKillersKills(killer, game);
        sendDeathMessages(player, killer, event);
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

    public void toteScore(Player player, GoalLocationInfo goal) {
        GameScore score = GameScore.getInstance();
        score.increment(MatchTeam.getTeam(player));
        score.updatePlayersGoals(player, game);
        game.addGoalInfo(player.getUniqueId());

        ChatBroadcasts.scoreMessage(game, player);
        BridgeFireworks fireworks = new BridgeFireworks(this);
        fireworks.spawnFireworks(player);
        if (!game.over()) {
            cagePlayers();
        } else {
            endGame();
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
    public void playerMove(PlayerMoveEvent event) {
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

        Bukkit.broadcastMessage("Welcome to the server " + player.getName() + "!");
        setGameModeForPlayer(player);
        resetPlayerHealthAndInventory(player);

        // set waiting board
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
        objective.getScore(TIMER_STRING).setScore(0);

        GameScore.initialize(board, objective, game);
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
                    this.cancel();
                    return;
                }
                if (game.getRemainingTimeInSeconds() < 0 ) {
                    this.cancel();
                    endGame();
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

    private void endGame(){
        game.setState(Game.GameState.AFTER_GAME);
        game.setEndTime();
        Bukkit.broadcastMessage("Game Over");
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
        JsonSerializer jsonSerializer = JsonSerializer.DEFAULT_READABLE;
        try {
            System.out.println(jsonSerializer.serialize(game));
        } catch (SerializeException e) {
            e.printStackTrace();
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
        System.out.println(foo);

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



