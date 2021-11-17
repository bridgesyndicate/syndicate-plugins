package gg.bridgesyndicate.bridgeteams;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class Inventory {
    private static final String SYNDICATE_ENV = System.getenv("SYNDICATE_ENV");
    private static final String BUCKET_NAME = "syndicate-" + SYNDICATE_ENV + "-bridge-kit-layouts";
    private final AmazonS3 s3Client;
    private HashMap<UUID, HashMap<String, Integer>> playerKitMap = new HashMap();

    Inventory() {
        s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(new DefaultAwsRegionProviderChain().getRegion())
                .build();
    }

    private String objectNameFromPlayerUUID(UUID uniqueId){
        return uniqueId.toString() + ".json";
    }

    public void addPlayer(Player player) {
        System.out.println("Adding kit for " + player.getUniqueId() + " from S3.");
        String playerKitJson = getKitJsonFromS3(player.getUniqueId());
        if (playerKitJson != null) {
            HashMap<String, Integer> playerHashMap = getPlayerHashMap(playerKitJson);
            if (playerHashMap != null)
                playerKitMap.put(player.getUniqueId(), playerHashMap);
        } else {
            System.out.println("No kit for " + player.getUniqueId() + " in S3.");
            return;
        }
    }

    public HashMap<String, Integer> getPlayerHashMap(String playerKitJson) {
        HashMap<String, Integer> kitLayoutMap = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            kitLayoutMap = mapper.readValue(playerKitJson, HashMap.class);
        } catch (IllegalArgumentException | JsonProcessingException e) {
            System.out.println("Error. Cannot deserialize players saved layout. Will use defaults.");
        }
        return kitLayoutMap;
    }

    private String getKitJsonFromS3(UUID uniqueId) {
        S3Object s3Object;
        try {
            s3Object = s3Client.getObject(BUCKET_NAME, objectNameFromPlayerUUID(uniqueId));
        } catch (Exception e) {
            return null;
        }
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        String text = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        return(text);
    }

    private int getInventoryLocation(Player player, String key, int defaultValue){
        HashMap<String, Integer> map = playerKitMap.get(player.getUniqueId());
        if (map == null)
            return defaultValue;
        return ( map.get(key) != null ) ? map.get(key) : defaultValue;
    }

    private ItemStack getIronSword() {
        ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
        ItemMeta itemMetaSword = ironSword.getItemMeta();
        itemMetaSword.spigot().setUnbreakable(true);
        ironSword.setItemMeta(itemMetaSword);
        return ironSword;
    }

    private ItemStack getBow() {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta itemMetaBow = bow.getItemMeta();
        itemMetaBow.setDisplayName(ChatColor.GREEN + "Bow");
        ArrayList<String> bowLore = new ArrayList<>();
        bowLore.add(ChatColor.GRAY + "Arrows regenerate every");
        bowLore.add(ChatColor.GREEN + "3.5s" + ChatColor.GRAY + ". You can have a maximum");
        bowLore.add(ChatColor.GRAY + "of " + ChatColor.GREEN + "1 " + ChatColor.GRAY + "arrow at a time.");
        bowLore.add("");
        itemMetaBow.setLore(bowLore);
        itemMetaBow.spigot().setUnbreakable(true);
        bow.setItemMeta(itemMetaBow);
        return bow;
    }

    private ItemStack getPickAxe() {
        ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta itemMetaPick = pick.getItemMeta();
        itemMetaPick.spigot().setUnbreakable(true);
        itemMetaPick.addEnchant(Enchantment.DIG_SPEED, 2, false);
        pick.setItemMeta(itemMetaPick);
        return pick;
    }

    private ItemStack getBlock(Player player) {
        ItemStack block = new ItemStack(Material.STAINED_CLAY, 64);
        block.setDurability(MatchTeam.getBlockColor(player));
        return(block);
    }

    private ItemStack getArrow() {
        ItemStack arrow = new ItemStack(Material.ARROW, 1);
        ItemMeta itemMetaArrow = arrow.getItemMeta();
        itemMetaArrow.setDisplayName(ChatColor.GREEN + "Arrow");
        ArrayList<String> arrowLore = new ArrayList<>();
        arrowLore.add(ChatColor.GRAY + "Regenerates every " + ChatColor.GREEN + "3.5s" + ChatColor.GRAY + "!");
        itemMetaArrow.setLore(arrowLore);
        arrow.setItemMeta(itemMetaArrow);
        return arrow;
    }

    private ItemStack getApple() {
        return new ItemStack(Material.GOLDEN_APPLE, 8);
    }

    public void setDefaultInventory(Player player) {
        fullyClearInventory(player);
        if(player.getGameMode().equals(GameMode.SPECTATOR)) return;
        player.getInventory().setItem(
                getInventoryLocation(player, "IRON_SWORD", 0),
                getIronSword());
        player.getInventory().setItem(
                getInventoryLocation(player, "BOW", 1),
                getBow());
        player.getInventory().setItem(
                getInventoryLocation(player, "DIAMOND_PICKAXE", 2),
                getPickAxe());
        player.getInventory().setItem(
                getInventoryLocation(player, "STAINED_CLAY0", 3),
                getBlock(player));
        player.getInventory().setItem(
                getInventoryLocation(player, "STAINED_CLAY1", 4),
                getBlock(player));
        player.getInventory().setItem(
                getInventoryLocation(player, "GOLDEN_APPLE", 5),
                getApple());
        player.getInventory().setItem(
                getInventoryLocation(player, "ARROW", 8),
                getArrow());

        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) chest.getItemMeta();
        leatherArmorMeta.setColor(MatchTeam.getArmorColor(player));
        leatherArmorMeta.setDisplayName(MatchTeam.getChatColor(player) + MatchTeam.getTeamName(player));
        leatherArmorMeta.spigot().setUnbreakable(true);
        chest.setItemMeta(leatherArmorMeta);
        player.getInventory().setChestplate(chest);
        ItemStack leg = new ItemStack(Material.LEATHER_LEGGINGS);
        leg.setItemMeta(leatherArmorMeta);
        player.getInventory().setLeggings(leg);
        ItemStack boot = new ItemStack(Material.LEATHER_BOOTS);
        boot.setItemMeta(leatherArmorMeta);
        player.getInventory().setBoots(boot);
        player.updateInventory();
    }

    public void fullyClearInventory(Player player){
        player.getInventory().clear();
        ItemStack clear = new ItemStack(Material.AIR);
        player.getOpenInventory().setCursor(clear);
        player.getOpenInventory().getTopInventory().clear();
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
    }

    // Arrow Regen

    public void reload(Player player) {
        player.getInventory().setItem(
                getInventoryLocation(player, "ARROW", 8),
                getArrow());
        player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1.0f, 1.0f);
    }
}
