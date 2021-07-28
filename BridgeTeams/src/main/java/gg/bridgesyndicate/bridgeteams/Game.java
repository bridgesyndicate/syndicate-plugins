package gg.bridgesyndicate.bridgeteams;


import org.apache.juneau.annotation.Beanc;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.parser.ParseException;
import org.apache.juneau.serializer.SerializeException;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Game {
    private final int requiredPlayers;
    private GameState state;
    private List redTeam;
    private List blueTeam;

    private HashSet joinedPlayers = new HashSet();

    public List getRedTeam() {
        return redTeam;
    }

    public List getBlueTeam() {
        return blueTeam;
    }

    public void setRedTeam(List redTeam) {
        this.redTeam = redTeam;
    }

    public void setBlueTeam(List blueTeam) {
        this.blueTeam = blueTeam;
    }

    public enum GameState { BEFORE_GAME, DURING_GAME, AFTER_GAME, CAGED };

    @Beanc(properties = "requiredPlayers,blueTeam,redTeam")
    public Game(int requiredPlayers, List blueTeam, List redTeam) {
        this.requiredPlayers = requiredPlayers;
        this.blueTeam = blueTeam;
        this.redTeam = redTeam;
        this.state = state.BEFORE_GAME;
    }

    public int getNumberOfJoinedPlayers() {
        return joinedPlayers.size();
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public static void main(String[] args) throws SerializeException, ParseException {
        Game myGame = new Game(2,
                Arrays.asList(new String[]{"KIIER"}),
                Arrays.asList(new String[]{"vice9"}));
        JsonSerializer jsonSerializer = JsonSerializer.DEFAULT_READABLE;
        String foo = jsonSerializer.serialize(myGame);
        JsonParser jsonParser = JsonParser.DEFAULT;
        Game dgame = jsonParser.parse(foo, Game.class);
        String foo2 = jsonSerializer.serialize(dgame);
        System.out.println(foo2);
    }

    public boolean hasPlayer(Player player) {
        return (getRedTeam().contains(player.getName()) ||
                getBlueTeam().contains(player.getName()));
    }

    public TeamType getTeam(Player player) {
        if (getRedTeam().contains(player.getName())) {
            return (TeamType.RED);
        } else {
            return (TeamType.BLUE);
        }
    }

    public void playerJoined(String name) {
        joinedPlayers.add(name);
    }
}
