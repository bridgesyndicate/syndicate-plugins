package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class LocationHelper {
    static Location makeLocation(World world, Vector vector, int yaw, int pitch) {
        Location location = new Location(world, vector.getX(), vector.getY(), vector.getZ(), yaw, pitch);
        return(location);
    }

    static BlockVector convertRedPlayerCageLocToActualRedCageLoc(Vector vector) {
        double x = vector.getX() + 0.5;
        double y = vector.getY() - 6;
        double z = vector.getZ() - 0.5;
        return(new BlockVector(x, y, z));
    }

    static BlockVector convertBluePlayerCageLocToActualBlueCageLoc(Vector vector) {
        double x = vector.getX() - 0.5;
        double y = vector.getY() - 6;
        double z = vector.getZ() - 0.5;
        return(new BlockVector(x, y, z));
    }
}
