package gg.bridgesyndicate.bridgeteams;

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

    public static List<String> getRedTeam() {
        return Team.redTeam;
    }

    public static List<String> getBlueTeam() {
        return Team.blueTeam;
    }

    public static List<String> getAllPlayersInTeams() {
        final List<String> combinedTeams = new ArrayList<String>();
        combinedTeams.addAll(Team.redTeam);
        combinedTeams.addAll(Team.blueTeam);
        return combinedTeams;
    }

    public static TeamType getTeamType(final Player player) {
        if (!isInTeam(player)) {
            return null;
        }
        return Team.redTeam.contains(player.getName()) ? TeamType.RED : TeamType.BLUE;
    }

    static {
        Team.redTeam = new ArrayList<String>();
        Team.blueTeam = new ArrayList<String>();
    }
}
