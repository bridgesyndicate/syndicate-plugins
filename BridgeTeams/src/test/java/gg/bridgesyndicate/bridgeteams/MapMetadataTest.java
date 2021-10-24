package gg.bridgesyndicate.bridgeteams;

import com.fasterxml.jackson.core.JsonProcessingException;
import gg.bridgesyndicate.util.BoundingBox;
import org.bukkit.util.Vector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapMetadataTest {

    @Test
    public void serialize() throws JsonProcessingException {
        MapMetadata mapMetadata = new MapMetadata();
        mapMetadata.setMapName("Aquatica");
        mapMetadata.setBuildLimits(new BoundingBox(-25, 84, -20, 25, 99, 20));
        mapMetadata.setRedGoalLocation(new BoundingBox(30, 83, -3, 36, 88, 3));
        mapMetadata.setBlueGoalLocation(new BoundingBox(-36, 83, -3, -30, 88, 3));
        mapMetadata.setRedRespawn(new Vector(29.5, 98, 0.5));
        mapMetadata.setBlueRespawn(new Vector(-28.5, 98, 0.5));
        mapMetadata.setRedCageLocation(new Vector(29.5, 102, 0.5));
        mapMetadata.setBlueCageLocation(new Vector(-28.5, 102, 0.5));
        String json = mapMetadata.serialize();

        MapMetadata newMapMetadata = MapMetadata.deserialize(json);
        assertEquals(mapMetadata.getMapName(), newMapMetadata.getMapName());
        assertEquals(mapMetadata.getRedRespawn(), newMapMetadata.getRedRespawn());
        assertEquals(mapMetadata.getBlueRespawn(), newMapMetadata.getBlueRespawn());
        assertEquals(mapMetadata.getRedCageLocation(), newMapMetadata.getRedCageLocation());
        assertEquals(mapMetadata.getBlueCageLocation(), newMapMetadata.getBlueCageLocation());
        assertEquals(mapMetadata.getBuildLimits(), newMapMetadata.getBuildLimits());
        assertEquals(mapMetadata.getRedGoalLocation(), newMapMetadata.getRedGoalLocation());
        assertEquals(mapMetadata.getBlueGoalLocation(), newMapMetadata.getBlueGoalLocation());
    }
}
