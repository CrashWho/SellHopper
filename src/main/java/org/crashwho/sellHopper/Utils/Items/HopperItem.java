package org.crashwho.sellHopper.Utils.Items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.crashwho.crashShop.CrashShop;
import org.crashwho.sellHopper.SellHopper;
import org.crashwho.sellHopper.Utils.ChatFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HopperItem {

    final SellHopper sellHopper;

    public HopperItem(SellHopper sellHopper) {
        this.sellHopper = sellHopper;
    }

    public ItemStack hopperItem() {
        ItemStack item = setupValue(Material.HOPPER, 0, "hopper");
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(sellHopper.getKey(), PersistentDataType.DOUBLE, 0d);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack inventoryItem(double amount) {
        Material material = Material.getMaterial(sellHopper.getConfig().getString("items.inventory.material"));
        return setupValue(material, amount, "inventory");
    }

    private ItemStack setupValue(Material material, double money, String key) {

        String displayname = sellHopper.getConfig().getString("items." + key + ".displayname");
        List<String> lore = sellHopper.getConfig().getStringList("items." + key + ".lore");
        float customModelData = (float) sellHopper.getConfig().getDouble("items." + key + ".custom-model-data");
        String itemModelkey = sellHopper.getConfig().getString("items." + key + ".item-model");
        NamespacedKey itemModel = (itemModelkey != null) ? NamespacedKey.fromString(itemModelkey) : null;

        boolean glint = sellHopper.getConfig().getBoolean("items." + key + ".glow", false);

        ConfigurationSection config = sellHopper.getConfig().getConfigurationSection("items." + key + ".enchantments");
        Map<String, Integer> enchantmentMap = new HashMap<>();

        if (config != null) {
            for (String name : config.getKeys(false)) {
                int level = config.getInt(name);
                enchantmentMap.put(name, level);
            }
        }

        return displayValue(material, displayname, money, lore, customModelData, itemModel, glint, enchantmentMap);
    }

    private ItemStack displayValue(Material material, String displayname, double money, List<String> lore, float customModelData, NamespacedKey itemModel, boolean glint, Map<String, Integer> enchantments) {
        ItemStack item = new ItemStack(material);

        if (displayname != null)
            item.setData(DataComponentTypes.CUSTOM_NAME, ChatFormat.format("<italic:false>" + displayname,
                    Placeholder.unparsed("money", CrashShop.getEconomy().format(money))));

        if (lore != null) {
            List<Component> loreComponents = new ArrayList<>();
            lore.forEach(l -> loreComponents.add(ChatFormat.format("<italic:false>" + l)));
            item.setData(DataComponentTypes.LORE, ItemLore.lore(loreComponents));
        }

        if (customModelData != 0) {
            item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData()
                    .addFloat(customModelData)
                    .build());
        }

        if (itemModel != null)
            item.setData(DataComponentTypes.ITEM_MODEL, itemModel);

        if (glint)
            item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint);

        if (enchantments != null) {
            Map<Enchantment, Integer> enchantmentMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                NamespacedKey key = NamespacedKey.fromString(entry.getKey());

                if (key != null) {
                    Registry<Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
                    Enchantment enchantment = enchantmentRegistry.get(key);
                    enchantmentMap.put(enchantment, entry.getValue());
                }
            }
            item.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(enchantmentMap));
        }

        return item;
    }

}
