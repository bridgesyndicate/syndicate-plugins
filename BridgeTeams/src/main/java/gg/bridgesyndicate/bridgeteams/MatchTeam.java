package gg.bridgesyndicate.bridgeteams;

import gg.bridgesyndicate.util.BoundingBox;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MatchTeam
{
    private static List<String> redTeam;
    private static List<String> blueTeam;

    private static MapMetadata mapMetadata = null;
    private final static World world = Bukkit.getWorld("world");

    public static void setMapMetadata(MapMetadata mapMetadata) {
        MatchTeam.mapMetadata = mapMetadata;
    }

    public static void addToTeam(final TeamType type, final Player player) {
        if (isInTeam(player)) {
            player.sendMessage(ChatColor.GRAY + "You are already on a team.");
            return;
        }
        switch (type) {
            case RED: {
                MatchTeam.redTeam.add(player.getName());
                break;
            }
            case BLUE: {
                MatchTeam.blueTeam.add(player.getName());
                break;
            }
        }
    }

    public static boolean isInTeam(final Player player) {
        return MatchTeam.redTeam.contains(player.getName()) || MatchTeam.blueTeam.contains(player.getName());
    }

    public static void clearTeams() {
        MatchTeam.redTeam.clear();
        MatchTeam.blueTeam.clear();
    }

    public static TeamType getTeam(final Player player) {
        return MatchTeam.redTeam.contains(player.getName()) ? TeamType.RED : TeamType.BLUE;
    }

    public static Color getArmorColor(final Player player) {
        return MatchTeam.redTeam.contains(player.getName()) ? Color.RED : Color.BLUE;
    }

    public static ChatColor getChatColor(final Player player) {
        return MatchTeam.redTeam.contains(player.getName()) ? ChatColor.RED : ChatColor.BLUE;
    }

    public static ChatColor getOpponentChatColor(final Player player) {
        return MatchTeam.redTeam.contains(player.getName()) ? ChatColor.BLUE : ChatColor.RED;
    }


    public static String getTeamName(final Player player) {
        return MatchTeam.redTeam.contains(player.getName()) ? "Red Team" : "Blue Team";
    }

    public static Location getSpawnLocation(final Player player) {
        final Location redLoc = LocationHelper.makeLocation(world, mapMetadata.getRedRespawn(), 90, 0);
        final Location blueLoc = LocationHelper.makeLocation(world, mapMetadata.getBlueRespawn(), -90, 0);
        return MatchTeam.redTeam.contains(player.getName()) ? redLoc : blueLoc;
    }

    public static Location getMapCenter() {
        return new Location(world, 0.5, 94.0, 0.5, 0, 0);
    }

    public static BlockVector getRedCageLocation() {
        return new BlockVector(LocationHelper.convertPlayerCageLocToActualCageLoc(mapMetadata.getRedCageLocation()));
    }

    public static BlockVector getBlueCageLocation() {
        return new BlockVector(LocationHelper.convertPlayerCageLocToActualCageLoc(mapMetadata.getBlueCageLocation()));
    }

    public static Location getRedPlayerCageLocation() {
        return(LocationHelper.makeLocation(world, mapMetadata.getRedCageLocation(), 90, 0));
    }

    public static Location getBluePlayerCageLocation() {
        return(LocationHelper.makeLocation(world, mapMetadata.getBlueCageLocation(), -90, 0));
    }

    public static BlockVector getCageLocation(TeamType team) {
        return team == TeamType.RED ? getRedCageLocation() : getBlueCageLocation();
    }

    public static Location getCagePlayerLocation(final Player player) {
        return MatchTeam.redTeam.contains(player.getName()) ? getRedPlayerCageLocation() : getBluePlayerCageLocation();
    }

    public static GoalLocationInfo getRedGoalMeta() {
        return (new GoalLocationInfo(mapMetadata.getBlueGoalLocation(), TeamType.BLUE, "Blue Goal")); //the goal that red players score on, which is on the blue side
    }

    public static GoalLocationInfo getBlueGoalMeta() {
        return (new GoalLocationInfo(mapMetadata.getRedGoalLocation(), TeamType.RED, "Red Goal")); //the goal that blue players score on, which is on the red side
    }

    public static BoundingBox getBuildLimits() {
        return(mapMetadata.getBuildLimits());
    }

    public static Collection<TeamType> getTeams(){
        ArrayList<TeamType> teams = new ArrayList<>();
        teams.add(TeamType.BLUE);
        teams.add(TeamType.RED);
        return(teams);
    }

    public static List<String> getRedTeam() {
        return redTeam;
    }

    public static List<String> getBlueTeam() {
        return blueTeam;
    }

    static {
        MatchTeam.redTeam = new ArrayList<>();
        MatchTeam.blueTeam = new ArrayList<>();
    }

    public static short getBlockColor(Player player) {
        return MatchTeam.redTeam.contains(player.getName()) ? (short) 14 : (short) 11;
    }

    public static String getScoreboardName(Player player) {
        return MatchTeam.redTeam.contains(player.getName()) ? "Red" : "Blue";
    }

    public static List<String> getPlayers(TeamType team) {
        if (team == TeamType.RED) {
            return (redTeam);
        } else {
            return (blueTeam);
        }
    }

    public static TeamType getOpposingTeam(TeamType team) {
        return (team == TeamType.RED) ? TeamType.BLUE : TeamType.RED;
    }

    public static ChatColor getChatColorForTeamType(TeamType teamType) {
        return (teamType == TeamType.RED) ? ChatColor.RED : ChatColor.BLUE;
    }
    
    public static boolean onSameTeam(Player playerOne, Player playerTwo) {
        return MatchTeam.getTeam(playerOne) == MatchTeam.getTeam(playerTwo);
    }
}

