package gg.bridgesyndicate.bridgeteams;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import gg.bridgesyndicate.util.ReadFile;
import gg.bridgesyndicate.util.Seconds;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import java.io.IOException;
import java.util.List;

public class Cages {
    private final BridgeTeams plugin;
    List<BridgeSchematicBlock> bridgeSchematicBlockList = null;
    private int[] cageSchematicIntegerList = null;
    private int cageSchematicIntegerListSize = 0;
    private enum action {CREATE, DESTROY}
    final float CAGED_DURATION = 5.0f;

    public Cages(BridgeTeams plugin) {
        this.plugin = plugin;
    }

    public void prepare() {
        try {
            final String schematicJson = ReadFile.read(ReadFile.pathToResources() + "cage.json");
            ObjectMapper objectMapper = new ObjectMapper();
            CollectionType typeReference =
                    TypeFactory.defaultInstance().constructCollectionType(List.class, BridgeSchematicBlock.class);
            bridgeSchematicBlockList = objectMapper.readValue(schematicJson, typeReference);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Exiting with error: Could not prepare cages.");
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
    private void buildOrDestroyCageAtLocation(BlockVector cageLocation, action createOrDestroy, TeamType team) {
        World world = Bukkit.getWorld("world");
        int cageX = cageLocation.getBlockX();
        int cageY = cageLocation.getBlockY();
        int cageZ = cageLocation.getBlockZ();
        for (int i = 0; i < cageSchematicIntegerListSize ; i++) {
            int id = (createOrDestroy.equals(action.CREATE)) ? cageSchematicIntegerList[i * 5 + 3] : 0;
            byte data = (createOrDestroy.equals(action.CREATE)) ? (byte) cageSchematicIntegerList[i * 5 + 4] : 0;
            if (team == TeamType.BLUE) { // turn the cage blue
                if (id == 100 && data == 0) {
                    id = 99;
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

    private void buildCages(action createOrDestroy) {
        eachCage(createOrDestroy);
        if (createOrDestroy.equals(action.DESTROY))
            return;
        destroyCagesAfterDelay();
    }

    private void eachCage(action createOrDestroy) {
        for (TeamType team : MatchTeam.getTeams()) {
            BlockVector cageLocation = MatchTeam.getCageLocation(team);
            buildOrDestroyCageAtLocation(cageLocation, createOrDestroy, team);
        }
    }

    private void destroyCagesAfterDelay() {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getGame().setState(Game.GameState.DURING_GAME);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1.0f, 1.0f);
                }
                buildCages(action.DESTROY);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if(!player.getGameMode().equals(GameMode.SPECTATOR)) {
                        plugin.setGameModeForPlayer(player);
                    }
                }
            }
        }.runTaskLater(plugin, Seconds.toTicks(CAGED_DURATION));
    }

    public void cagePlayers(boolean isStartOfGame) {
        plugin.getGame().setState(Game.GameState.CAGED);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(!player.getGameMode().equals(GameMode.SPECTATOR)){
                player.teleport(MatchTeam.getCagePlayerLocation(player));
                plugin.cagedPlayerCountdownTitles(player);
                plugin.resetPlayerHealthAndInventory(player);
                plugin.setGameModeForPlayer(player);
            }
        }
        if (isStartOfGame) {
            destroyCagesAfterDelay();
        } else {
            buildCages(action.CREATE);
        }
    }

    public void build() {
        eachCage(action.CREATE);
    }
}
