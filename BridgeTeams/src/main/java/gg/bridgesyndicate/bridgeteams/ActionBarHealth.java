//package gg.bridgesyndicate.bridgeteams;
//
//import net.minecraft.server.v1_8_R3.IChatBaseComponent;
//import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
//import org.bukkit.ChatColor;
//import org.bukkit.entity.Player;
//
//
//public class ActionBarHealth {
//
//    private PacketPlayOutChat packet;
//
//    public ActionBarHealth(String text) {
//        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + text + "\"}"), (byte) 2);
//        this.packet = packet;
//    }
//
//    public void sendToPlayer(Player p) {
//        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
//    }
//
//    public static String formatDamage(int dmg){
//
//        String damage;
//
//        switch(dmg){
//
//            case 10: {
//                damage = ChatColor.RED + "❤❤❤❤❤❤❤❤❤❤";
//                break;
//            }
//            case 9: {
//                damage = ChatColor.RED + "❤❤❤❤❤❤❤❤❤";
//                break;
//            }
//            case 8: {
//                damage = ChatColor.RED + "❤❤❤❤❤❤❤❤";
//                break;
//            }
//            case 7: {
//                damage = ChatColor.RED + "❤❤❤❤❤❤❤";
//                break;
//            }
//            case 6: {
//                damage = ChatColor.RED + "❤❤❤❤❤❤";
//                break;
//            }
//            case 5: {
//                damage = ChatColor.RED + "❤❤❤❤❤";
//                break;
//            }
//            case 4: {
//                damage = ChatColor.RED + "❤❤❤❤";
//                break;
//            }
//            case 3: {
//                damage = ChatColor.RED + "❤❤❤";
//                break;
//            }
//            case 2: {
//                damage = ChatColor.RED + "❤❤";
//                break;
//            }
//            case 1: {
//                damage = ChatColor.RED + "❤";
//                break;
//            }
//            case 0: {
//                damage = "";
//                break;
//            }
//            default: {
//                damage = "Unexpected Value";
//                break;
//            }
//        }
//
//        return damage;
//    }
//
//    public static String formatHealth(int health){
//
//        String hearts;
//
//        switch(health){
//
//            case 10: {
//                hearts = ChatColor.DARK_RED + "❤❤❤❤❤❤❤❤❤❤";
//                break;
//            }
//            case 9: {
//                hearts = ChatColor.DARK_RED + "❤❤❤❤❤❤❤❤❤";
//                break;
//            }
//            case 8: {
//                hearts = ChatColor.DARK_RED + "❤❤❤❤❤❤❤❤";
//                break;
//            }
//            case 7: {
//                hearts = ChatColor.DARK_RED + "❤❤❤❤❤❤❤";
//                break;
//            }
//            case 6: {
//                hearts = ChatColor.DARK_RED + "❤❤❤❤❤❤";
//                break;
//            }
//            case 5: {
//                hearts = ChatColor.DARK_RED + "❤❤❤❤❤";
//                break;
//            }
//            case 4: {
//                hearts = ChatColor.DARK_RED + "❤❤❤❤";
//                break;
//            }
//            case 3: {
//                hearts = ChatColor.DARK_RED + "❤❤❤";
//                break;
//            }
//            case 2: {
//                hearts = ChatColor.DARK_RED + "❤❤";
//                break;
//            }
//            case 1: {
//                hearts = ChatColor.DARK_RED + "❤";
//                break;
//            }
//            case 0: {
//                hearts = "";
//                break;
//            }
//            default: {
//                hearts = "Unexpected Value";
//                break;
//            }
//        }
//
//        return hearts;
//    }
//
//    public static String formatBlackHearts(int blackHealth){
//
//        String blackHearts;
//
//        switch(blackHealth){
//
//            case 10: {
//                blackHearts = ChatColor.BLACK + "❤❤❤❤❤❤❤❤❤❤";
//                break;
//            }
//            case 9: {
//                blackHearts = ChatColor.BLACK + "❤❤❤❤❤❤❤❤❤";
//                break;
//            }
//            case 8: {
//                blackHearts = ChatColor.BLACK + "❤❤❤❤❤❤❤❤";
//                break;
//            }
//            case 7: {
//                blackHearts = ChatColor.BLACK + "❤❤❤❤❤❤❤";
//                break;
//            }
//            case 6: {
//                blackHearts = ChatColor.BLACK + "❤❤❤❤❤❤";
//                break;
//            }
//            case 5: {
//                blackHearts = ChatColor.BLACK + "❤❤❤❤❤";
//                break;
//            }
//            case 4: {
//                blackHearts = ChatColor.BLACK + "❤❤❤❤";
//                break;
//            }
//            case 3: {
//                blackHearts = ChatColor.BLACK + "❤❤❤";
//                break;
//            }
//            case 2: {
//                blackHearts = ChatColor.BLACK + "❤❤";
//                break;
//            }
//            case 1: {
//                blackHearts = ChatColor.BLACK + "❤";
//                break;
//            }
//            case 0: {
//                blackHearts = "";
//                break;
//            }
//            default: {
//                blackHearts = "Unexpected Value";
//                break;
//            }
//        }
//
//        return blackHearts;
//
//    }
//
//    public static String formatGoldHearts(int absInt) {
//
//        String goldHearts;
//
//        switch(absInt){
//            case 4: {
//                goldHearts = ChatColor.YELLOW + "❤❤";
//                break;
//            }
//            case 3: {
//                goldHearts = ChatColor.YELLOW + "❤" + ChatColor.GOLD + "❤";
//                break;
//            }
//            case 2: {
//                goldHearts = ChatColor.YELLOW + "❤";
//                break;
//            }
//            case 1: {
//                goldHearts = ChatColor.GOLD + "❤";
//                break;
//            }
//            case 0: {
//                goldHearts = "";
//                break;
//            }
//            default: {
//                goldHearts = ChatColor.YELLOW + "Unexpected Value";
//                break;
//            }
//        }
//
//        return goldHearts;
//    }
//
//}
