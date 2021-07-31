package gg.bridgesyndicate.bridgeteams;
import gg.bridgesyndicate.util.BoundingBox;

public class GoalMeta {
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

    public GoalMeta(BoundingBox boundingBox, TeamType team, String goalName) {
        this.boundingBox = boundingBox;
        this.goalName = goalName;
        this.team = team;
    }
}
