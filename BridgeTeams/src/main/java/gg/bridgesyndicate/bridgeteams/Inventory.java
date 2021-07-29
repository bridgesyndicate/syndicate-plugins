package gg.bridgesyndicate.bridgeteams;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;

public class Inventory {

    public static void setDefaultInventory(Player player) {
        // HOTBAR
        player.getInventory().clear();
        ItemStack ironSword = new ItemStack(Material.IRON_SWORD);

        ItemMeta itemMetaSword = ironSword.getItemMeta();
        itemMetaSword.spigot().setUnbreakable(true);
        ironSword.setItemMeta(itemMetaSword);


        player.getInventory().setItem(0, ironSword);
        player.updateInventory();

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

        player.getInventory().setItem(1, bow);
        player.updateInventory();

        ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE);

        ItemMeta itemMetaPick = pick.getItemMeta();
        itemMetaPick.spigot().setUnbreakable(true);
        itemMetaPick.addEnchant(Enchantment.DIG_SPEED, 2, false);
        pick.setItemMeta(itemMetaPick);

        player.getInventory().setItem(2, pick);
        player.updateInventory();

        ItemStack blocks1 = new ItemStack(Material.STAINED_CLAY, 64);

        blocks1.setDurability(MatchTeam.getBlockColor(player));

        player.getInventory().setItem(3, blocks1);
        player.getInventory().setItem(4, blocks1);
        player.updateInventory();

        ItemStack gaps = new ItemStack(Material.GOLDEN_APPLE, 8);

        player.getInventory().setItem(5, gaps);
        player.updateInventory();

        ItemStack arrow = new ItemStack(Material.ARROW, 1);

        ItemMeta itemMetaArrow = arrow.getItemMeta();
        itemMetaArrow.setDisplayName(ChatColor.GREEN + "Arrow");

        ArrayList<String> arrowLore = new ArrayList<>();
        arrowLore.add(ChatColor.GRAY + "Regenerates every " + ChatColor.GREEN + "3.5s" + ChatColor.GRAY + "!");
        itemMetaArrow.setLore(arrowLore);

        arrow.setItemMeta(itemMetaArrow);
        player.getInventory().setItem(8, arrow);
        player.updateInventory();

        //ARMOR SLOTS

        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);

        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) chest.getItemMeta();
        leatherArmorMeta.setColor(MatchTeam.getArmorColor(player));
        leatherArmorMeta.setDisplayName(MatchTeam.getChatColor(player) + MatchTeam.getTeamName(player));
        leatherArmorMeta.spigot().setUnbreakable(true);

        chest.setItemMeta(leatherArmorMeta);
        player.getInventory().setChestplate(chest);
        player.updateInventory();

        ItemStack leg = new ItemStack(Material.LEATHER_LEGGINGS);

        leg.setItemMeta(leatherArmorMeta);
        player.getInventory().setLeggings(leg);
        player.updateInventory();

        ItemStack boot = new ItemStack(Material.LEATHER_BOOTS);

        boot.setItemMeta(leatherArmorMeta);
        player.getInventory().setBoots(boot);
        player.updateInventory();

    }
}
