package me.bdamja.bridgerespawn;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.*;
import org.bukkit.plugin.*;
import org.bukkit.event.entity.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;

public final class BridgeRespawn extends JavaPlugin implements Listener {
    private Object PlayerDeathEvent;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents((Listener) this, (Plugin) this);
    }

    @EventHandler
    public void onDeathOfPlayer(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.setHealth(20.0);
        Location redLoc = new Location(Bukkit.getWorld("world"), 28.5, 98, 0.5, 90, 0);
        player.teleport(redLoc);
        event.getDrops().clear();

        String playerName = player.getName();
        event.setDeathMessage(ChatColor.RED + playerName + ChatColor.GRAY + " was killed by " + ChatColor.BLUE + "bluePlayer" + ChatColor.GRAY + ".");
        event.setDroppedExp(0);

        player.getInventory().clear();
        for(PotionEffect effect:player.getActivePotionEffects()){
            player.removePotionEffect(effect.getType());
        }

        // HOTBAR

        ItemStack ironSword = new ItemStack(Material.IRON_SWORD);

        ItemMeta itemMetaSword = ironSword.getItemMeta();
        itemMetaSword.spigot().setUnbreakable(true);
        ironSword.setItemMeta(itemMetaSword);


        player.getInventory().setItem(0, ironSword);
        player.updateInventory();

        ItemStack bow = new ItemStack(Material.BOW);

        ItemMeta itemMetaBow = bow.getItemMeta();
        itemMetaBow.setDisplayName(ChatColor.GREEN + "Bow");

        ArrayList<String> bowLore = new ArrayList<String>();
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

        blocks1.setDurability((short) 14);

        player.getInventory().setItem(3, blocks1);
        player.getInventory().setItem(4, blocks1);
        player.updateInventory();

        ItemStack gaps = new ItemStack(Material.GOLDEN_APPLE, 8);

        player.getInventory().setItem(5, gaps);
        player.updateInventory();

        ItemStack arrow = new ItemStack(Material.ARROW, 1);

        ItemMeta itemMetaArrow = arrow.getItemMeta();
        itemMetaArrow.setDisplayName(ChatColor.GREEN + "Arrow");

        ArrayList<String> arrowLore = new ArrayList<String>();
        arrowLore.add(ChatColor.GRAY + "Regenerates every " + ChatColor.GREEN + "3.5s" + ChatColor.GRAY + "!");
        itemMetaArrow.setLore(arrowLore);

        arrow.setItemMeta(itemMetaArrow);
        player.getInventory().setItem(8, arrow);
        player.updateInventory();

        //ARMOR SLOTS

        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);

        LeatherArmorMeta red = (LeatherArmorMeta)chest.getItemMeta();
        red.setColor(Color.fromRGB(255,0,0));
        red.setDisplayName(ChatColor.RED + "Red Team");

        chest.setItemMeta(red);
        player.getInventory().setChestplate(chest);
        player.updateInventory();

        ItemStack leg = new ItemStack(Material.LEATHER_LEGGINGS);

        leg.setItemMeta(red);
        player.getInventory().setLeggings(leg);
        player.updateInventory();

        ItemStack boot = new ItemStack(Material.LEATHER_BOOTS);

        boot.setItemMeta(red);
        player.getInventory().setBoots(boot);
        player.updateInventory();
    }

    @EventHandler

    public void playerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        String playerName = player.getName();
        if (player.getLocation().getY() < 83) {
            Location redLoc = new Location(Bukkit.getWorld("world"), 28.5, 98, 0.5, 90, 0);
            player.teleport(redLoc);
            player.sendMessage(ChatColor.RED + playerName + ChatColor.GRAY + " fell into the void.");
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20);

            player.getInventory().clear();
            for(PotionEffect effect:player.getActivePotionEffects()){
                player.removePotionEffect(effect.getType());
            }

            // HOTBAR

            ItemStack ironSword = new ItemStack(Material.IRON_SWORD);

            ItemMeta itemMetaSword = ironSword.getItemMeta();
            itemMetaSword.spigot().setUnbreakable(true);
            ironSword.setItemMeta(itemMetaSword);


            player.getInventory().setItem(0, ironSword);
            player.updateInventory();

            ItemStack bow = new ItemStack(Material.BOW);

            ItemMeta itemMetaBow = bow.getItemMeta();
            itemMetaBow.setDisplayName(ChatColor.GREEN + "Bow");

            ArrayList<String> bowLore = new ArrayList<String>();
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

            blocks1.setDurability((short) 14);

            player.getInventory().setItem(3, blocks1);
            player.getInventory().setItem(4, blocks1);
            player.updateInventory();

            ItemStack gaps = new ItemStack(Material.GOLDEN_APPLE, 8);

            player.getInventory().setItem(5, gaps);
            player.updateInventory();

            ItemStack arrow = new ItemStack(Material.ARROW, 1);

            ItemMeta itemMetaArrow = arrow.getItemMeta();
            itemMetaArrow.setDisplayName(ChatColor.GREEN + "Arrow");

            ArrayList<String> arrowLore = new ArrayList<String>();
            arrowLore.add(ChatColor.GRAY + "Regenerates every " + ChatColor.GREEN + "3.5s" + ChatColor.GRAY + "!");
            itemMetaArrow.setLore(arrowLore);

            arrow.setItemMeta(itemMetaArrow);
            player.getInventory().setItem(8, arrow);
            player.updateInventory();

            //ARMOR SLOTS

            ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);

            LeatherArmorMeta red = (LeatherArmorMeta)chest.getItemMeta();
            red.setColor(Color.fromRGB(255,0,0));
            red.setDisplayName(ChatColor.RED + "Red Team");
            red.spigot().setUnbreakable(true);

                chest.setItemMeta(red);
                player.getInventory().setChestplate(chest);
                player.updateInventory();

            ItemStack leg = new ItemStack(Material.LEATHER_LEGGINGS);

                leg.setItemMeta(red);
                player.getInventory().setLeggings(leg);
                player.updateInventory();

            ItemStack boot = new ItemStack(Material.LEATHER_BOOTS);

                boot.setItemMeta(red);
                player.getInventory().setBoots(boot);
                player.updateInventory();
    }


        }
    }

