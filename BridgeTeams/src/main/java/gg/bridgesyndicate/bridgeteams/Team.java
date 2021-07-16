package gg.bridgesyndicate.bridgeteams;

import gg.bridgesyndicate.util.BoundingBox;
import org.bukkit.entity.*;
import org.bukkit.*;
import java.util.*;

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
        if (!isInTeam(player)) {
            return null;
        }
        return Team.redTeam.contains(player.getName()) ? TeamType.RED : TeamType.BLUE;
    }

    public static Color getArmorColor(final Player player) {
        if (!isInTeam(player)) {
            return null;
        }
        return Team.redTeam.contains(player.getName()) ? Color.RED : Color.BLUE;
    }

    public static ChatColor getChatColor(final Player player) {
        if (!isInTeam(player)) {
            return null;
        }
        return Team.redTeam.contains(player.getName()) ? ChatColor.RED : ChatColor.BLUE;
    }

    public static String getTeamName(final Player player) {
        if (!isInTeam(player)) {
            return null;
        }
        return Team.redTeam.contains(player.getName()) ? "Red Team" : "Blue Team";
    }

    public static Location getSpawnLocation(final Player player) {
        if (!isInTeam(player)) {
            return null;
        }

        final Location redLoc = new Location(Bukkit.getWorld("world"), 28.5, 98, 0.5, 90, 0);
        final Location blueLoc = new Location(Bukkit.getWorld("world"), -27.5, 98, 0.5, -90, 0);

        return Team.redTeam.contains(player.getName()) ? redLoc : blueLoc;
    }

    public static Location getCageLocation(final Player player) {
        if (!isInTeam(player)) {
            return null;
        }

        final Location redCage = new Location(Bukkit.getWorld("world"), 28.5, 98, 0.5, 90, 0);
        final Location blueCage = new Location(Bukkit.getWorld("world"), -27.5, 98, 0.5, -90, 0);

        return Team.redTeam.contains(player.getName()) ? redCage : blueCage;
    }

    public static GoalMeta getRedGoalMeta() {
        return (new GoalMeta(new BoundingBox(-30, 83, 3, -36, 88, -3), TeamType.BLUE, "Blue Goal"));
    }

    public static GoalMeta getBlueGoalMeta() {
        return (new GoalMeta( new BoundingBox(30,83,-3,36,88,3), TeamType.RED, "Red Goal"));
    }

    static {
        Team.redTeam = new ArrayList<>();
        Team.blueTeam = new ArrayList<>();
    }
}

