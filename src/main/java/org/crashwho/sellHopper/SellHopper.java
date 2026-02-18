package org.crashwho.sellHopper;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.crashwho.crashShop.api.CrashShopAPI;
import org.crashwho.crashShop.api.ShopItem;
import org.crashwho.sellHopper.Commands.GiveItem;
import org.crashwho.sellHopper.Events.BlockEvent;
import org.crashwho.sellHopper.Events.InventoryEvent;
import org.crashwho.sellHopper.Utils.ChatFormat;
import org.crashwho.sellHopper.Utils.Files.MessagesManager;

import java.util.HashMap;
import java.util.Map;

public final class SellHopper extends JavaPlugin {

    private CrashShopAPI crashShopAPI;
    private NamespacedKey key;
    private MessagesManager messagesManager;

    private final Map<Material, ShopItem> shopCache = new HashMap<>();

    @Override
    public void onEnable() {
        setupFiles();
        if (!setupAPI()) {
            getLogger().severe("CrashShop not found! Please install the plugin on your server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        key = new NamespacedKey(this, "sellhopper");
        registerCmd();
        loadCache();
        registerEvent();
    }

    private boolean setupAPI() {
        RegisteredServiceProvider<CrashShopAPI> rsp = getServer().getServicesManager().getRegistration(CrashShopAPI.class);
        if (rsp == null) {
            return false;
        }
        this.crashShopAPI = rsp.getProvider();
        return crashShopAPI != null;
    }

    private void registerCmd() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, cmd -> {
            cmd.registrar().register(new GiveItem(this).hopperCommand());
        });
    }

    private void registerEvent() {
        Bukkit.getPluginManager().registerEvents(new BlockEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryEvent(this), this);
    }

    private void setupFiles() {
        saveResource("config.yml", false);
        messagesManager = new MessagesManager(this);
        ChatFormat.init(this);
    }

    private void loadCache() {
        shopCache.clear();
        for (ShopItem shopItem : getCrashShopAPI().getShopItems()) {
            shopCache.put(shopItem.getItem().getType(), shopItem);
        }
    }

    public CrashShopAPI getCrashShopAPI() {
        return crashShopAPI;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public void reloadFiles() {
        reloadConfig();
        messagesManager.reloadMessages();
        loadCache();
        ChatFormat.init(this);
    }

    public Map<Material, ShopItem> getShopCache() {
        return shopCache;
    }

    public FileConfiguration getMessages() {
        return messagesManager.getData();
    }
}
