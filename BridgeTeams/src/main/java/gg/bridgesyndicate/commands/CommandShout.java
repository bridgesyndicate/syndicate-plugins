package gg.bridgesyndicate.commands;

import gg.bridgesyndicate.bridgeteams.ChatBroadcasts;
import gg.bridgesyndicate.bridgeteams.ChatHandler;
import gg.bridgesyndicate.bridgeteams.MatchTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CommandShout implements CommandExecutor {

    private static final String MUST_BE_PLAYER = ChatColor.RED + "You must be a player to use /shout.";
    private static final String SHOUT_PREFIX = ChatColor.GOLD + "[SHOUT] ";
    private static final String WHITE_COLON = ChatColor.WHITE + ": ";

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player sender = (Player) commandSender;
            if (sender.getGameMode().equals(GameMode.SPECTATOR)) {
                sender.sendMessage(MUST_BE_PLAYER);
            } else {
                if (Arrays.stream(strings).findAny().isPresent()) { // `strings` is an array of arguments the player gave in their usage of /shout
                    String senderName = MatchTeam.getChatColor(sender) + sender.getName();
                    String message = String.join(" ", strings);
                    String content = SHOUT_PREFIX + senderName + WHITE_COLON + message;
                    Bukkit.broadcastMessage(content);
                } else {
                    return false; // returning false tells the user how to perform the command properly
                }
            }
        }
        return true;
    }
}
