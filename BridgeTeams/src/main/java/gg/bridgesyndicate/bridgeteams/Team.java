package gg.bridgesyndicate.bridgeteams;

import gg.bridgesyndicate.util.BoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Team
{
    private static List<String> redTeam;
    private static List<String> blueTeam;

    public static void addToTeam(final TeamType type, final Player player) {
        if (isInTeam(player)) {
            player.sendMessage(ChatColor.GRAY + "You are already on a team.");
            return;
        }
        switch (type) {
            case RED: {
                Team.redTeam.add(player.getName());
                break;
            }
            case BLUE: {
                Team.blueTeam.add(player.getName());
                break;
            }
        }
        player.sendMessage(ChatColor.GRAY + "You are now on the " + ChatColor.GRAY + type.name() + ChatColor.GRAY + " team!");
    }

    public static boolean isInTeam(final Player player) {
        return Team.redTeam.contains(player.getName()) || Team.blueTeam.contains(player.getName());
    }

    public static void clearTeams() {
        Team.redTeam.clear();
        Team.blueTeam.clear();
    }

    public static TeamType getTeam(final Player player) {
        return Team.redTeam.contains(player.getName()) ? TeamType.RED : TeamType.BLUE;
    }

    public static Color getArmorColor(final Player player) {
        return Team.redTeam.contains(player.getName()) ? Color.RED : Color.BLUE;
    }

    public static ChatColor getChatColor(final Player player) {
        return Team.redTeam.contains(player.getName()) ? ChatColor.RED : ChatColor.BLUE;
    }

    public static ChatColor getOpponentChatColor(final Player player) {
        return Team.redTeam.contains(player.getName()) ? ChatColor.BLUE : ChatColor.RED;
    }


    public static String getTeamName(final Player player) {
        return Team.redTeam.contains(player.getName()) ? "Red Team" : "Blue Team";
    }

    public static Location getSpawnLocation(final Player player) {
        final Location redLoc = new Location(Bukkit.getWorld("world"), 28.5, 98, 0.5, 90, 0);
        final Location blueLoc = new Location(Bukkit.getWorld("world"), -27.5, 98, 0.5, -90, 0);

        return Team.redTeam.contains(player.getName()) ? redLoc : blueLoc;
    }

    public static Location getRedCageLocation() {
        return(new Location(Bukkit.getWorld("world"), 28.5, 104, 0.5, 90, 0));
    }

    public static Location getBlueCageLocation() {
        return(new Location(Bukkit.getWorld("world"), -27.5, 104, 0.5, -90, 0));
    }

    public static Location getRedPlayerCageLocation() {
        return(new Location(Bukkit.getWorld("world"), 28.5, 105, 0.5, 90, 0));
    }

    public static Location getBluePlayerCageLocation() {
        return(new Location(Bukkit.getWorld("world"), -27.5, 105, 0.5, -90, 0));
    }

    public static Location getCageLocation(TeamType team) {
        return team == TeamType.RED ? getRedCageLocation() : getBlueCageLocation();
    }

    public static Location getCagePlayerLocation(final Player player) {
        return Team.redTeam.contains(player.getName()) ? getRedPlayerCageLocation() : getBluePlayerCageLocation();
    }

    public static GoalMeta getRedGoalMeta() {
        return (new GoalMeta(new BoundingBox(-30, 83, 3, -36, 88, -3), TeamType.BLUE, "Blue Goal"));
    }

    public static GoalMeta getBlueGoalMeta() {
        return (new GoalMeta( new BoundingBox(30,83,-3,36,88,3), TeamType.RED, "Red Goal"));
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
        Team.redTeam = new ArrayList<>();
        Team.blueTeam = new ArrayList<>();
    }

    public static short getBlockColor(Player player) {
        return Team.redTeam.contains(player.getName()) ? (short) 14 : (short) 11;
    }

    public static String getScoreboardName(Player player) {
        return Team.redTeam.contains(player.getName()) ? "Red" : "Blue";
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
}

