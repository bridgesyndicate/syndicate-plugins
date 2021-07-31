package gg.bridgesyndicate.bridgeteams;
import gg.bridgesyndicate.util.BoundingBox;

public class GoalLocationInfo {
    private final BoundingBox boundingBox;
    private final String goalName;

    private final TeamType team;

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public String getGoalName() {
        return goalName;
    }

    public TeamType getTeam() {
        return team;
    }

    public GoalLocationInfo(BoundingBox boundingBox, TeamType team, String goalName) {
        this.boundingBox = boundingBox;
        this.goalName = goalName;
        this.team = team;
    }
}
