package gg.bridgesyndicate.bridgeteams;

import java.util.ArrayList;
import java.util.List;

public class BridgeGoals {
    public static List<GoalLocationInfo> getGoalList() {
        final List<GoalLocationInfo> goalList = new ArrayList<>();
        goalList.add(MatchTeam.getBlueGoalMeta());
        goalList.add(MatchTeam.getRedGoalMeta());
        return(goalList);
    }
}
