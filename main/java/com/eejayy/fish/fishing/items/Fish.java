package com.eejayy.fish.fishing.items;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class Fish {

    String name;
    Rarities rarity;
    Material type;
    Player fisherman;
    Float length;

    // @TODO add biome-rarity support

    public Fish(Rarities rarity, Player fisher) {
        this.rarity = rarity;
        this.name = Names.get(rarity);
        this.type = Material.COD;
        this.fisherman = fisher;
        this.length = 5.0f;
    }

    // Using translate method over the enum values for the sake of a future config file.

    public ItemStack getItem() {
        ItemStack fish = new ItemStack(type);
        ItemMeta fishMeta = fish.getItemMeta();
        List<String> lore = Arrays.asList(
                ChatColor.WHITE + "Fished by " + fisherman.getName(),
                ChatColor.WHITE + "Weighs " + Float.toString(length) + "kg",
                " ",
                ChatColor.translateAlternateColorCodes('&', rarity.getCode()) + rarity.toString()

        );

        fishMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', rarity.getCode()) + name);
        fishMeta.setLore(lore);
        fish.setItemMeta(fishMeta);

        return fish;
    }
}
