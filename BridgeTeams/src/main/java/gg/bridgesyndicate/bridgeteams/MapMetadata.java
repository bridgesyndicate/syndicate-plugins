package gg.bridgesyndicate.bridgeteams;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import gg.bridgesyndicate.util.BoundingBox;
import org.bukkit.util.Vector;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MapMetadata {

    private String mapName;
    private Vector redRespawn;
    private Vector blueRespawn;
    private Vector redCageLocation;
    private Vector blueCageLocation;
    private BoundingBox buildLimits;
    private BoundingBox redGoalLocation;
    private BoundingBox blueGoalLocation;

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public Vector getRedRespawn() {
        return redRespawn;
    }

    public void setRedRespawn(Vector redRespawn) {
        this.redRespawn = redRespawn;
    }

    public Vector getBlueRespawn() {
        return blueRespawn;
    }

    public void setBlueRespawn(Vector blueRespawn) {
        this.blueRespawn = blueRespawn;
    }

    public Vector getRedCageLocation() {
        return redCageLocation;
    }

    public void setRedCageLocation(Vector redCageLocation) {
        this.redCageLocation = redCageLocation;
    }

    public Vector getBlueCageLocation() {
        return blueCageLocation;
    }

    public void setBlueCageLocation(Vector blueCageLocation) {
        this.blueCageLocation = blueCageLocation;
    }

    public BoundingBox getBuildLimits() { return buildLimits;  }

    public void setBuildLimits(BoundingBox buildLimits) { this.buildLimits = buildLimits; }

    public BoundingBox getRedGoalLocation() { return redGoalLocation; }

    public void setRedGoalLocation(BoundingBox redGoalLocation) { this.redGoalLocation = redGoalLocation; }

    public BoundingBox getBlueGoalLocation() { return blueGoalLocation; }

    public void setBlueGoalLocation(BoundingBox blueGoalLocation) { this.blueGoalLocation = blueGoalLocation; }

    String serialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper
                .addMixIn(Vector.class, VectorSerializeMixIn.class)
                .addMixIn(BoundingBox.class, BoundingBoxSerializeMixIn.class);
        return(mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(this));
    }

    public static MapMetadata deserialize(String json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Vector.class, VectorDeserializeMixIn.class);
        MapMetadata mapMetadata = null;
        try {
            mapMetadata = mapper.readValue(json, MapMetadata.class);
        } catch (JsonProcessingException e) {
            System.err.println("Cannot parse json.");
            e.printStackTrace();
        }
        return mapMetadata;
    }

}

abstract class BoundingBoxSerializeMixIn{
    @JsonIgnore public abstract Vector getMin();
    @JsonIgnore public abstract Vector getMax();
    @JsonIgnore public abstract Vector getCenter();
    @JsonIgnore public abstract double getWidthX();
    @JsonIgnore public abstract double getWidthZ();
    @JsonIgnore public abstract double getHeight();
    @JsonIgnore public abstract double getVolume();
    @JsonIgnore public abstract double getCenterX();
    @JsonIgnore public abstract double getCenterY();
    @JsonIgnore public abstract double getCenterZ();
}

abstract class VectorDeserializeMixIn {
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS)
    @JsonIgnore public abstract Vector setX(int val);
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS)
    @JsonIgnore public abstract Vector setX(double val);
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS)
    @JsonIgnore public abstract Vector setY(int val);
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS)
    @JsonIgnore public abstract Vector setY(double val);
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS)
    @JsonIgnore public abstract Vector setZ(int val);
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS)
    @JsonIgnore public abstract Vector setZ(double val);
}

abstract class VectorSerializeMixIn {
    @JsonIgnore public abstract int getBlockX();
    @JsonIgnore public abstract int getBlockY();
    @JsonIgnore public abstract int getBlockZ();
}