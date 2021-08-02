package gg.bridgesyndicate.bridgeteams;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class BridgeTitles {
    public static List<String> getFinalTitles() {
        TeamType winningTeam;
        int winningTeamScore;
        int losingTeamScore;
        ChatColor winningTeamChatColor;
        ChatColor losingTeamChatColor;
        String finalTitle;
        String finalScore;

        if (GameScore.getBlue() == GameScore.getRed()) {
            winningTeamScore = losingTeamScore = GameScore.getRed();
            winningTeamChatColor = ChatColor.YELLOW;
            losingTeamChatColor = ChatColor.YELLOW;
            finalTitle = ChatColor.YELLOW + "TIE!";
        } else {
            if (GameScore.getBlue() > GameScore.getRed()) {
                winningTeam = TeamType.BLUE;
                winningTeamScore = GameScore.getBlue();
                losingTeamScore = GameScore.getRed();
            } else {
                winningTeam = TeamType.RED;
                winningTeamScore = GameScore.getRed();
                losingTeamScore = GameScore.getBlue();
            }
            winningTeamChatColor = MatchTeam.getChatColorForTeamType(winningTeam);
            losingTeamChatColor = MatchTeam.getChatColorForTeamType(MatchTeam.getOpposingTeam(winningTeam));
            finalTitle = winningTeamChatColor + winningTeam.name() + " WINS!";
        }
        finalScore = winningTeamChatColor + "" + winningTeamScore
                + ChatColor.GRAY + " - " + losingTeamChatColor + "" + losingTeamScore;
        ArrayList<String> returnValue = new ArrayList<>();
        returnValue.add(finalTitle);
        returnValue.add(finalScore);
        return(returnValue);
    }
}
