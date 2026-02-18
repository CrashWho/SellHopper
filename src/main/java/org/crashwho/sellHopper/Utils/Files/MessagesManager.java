package org.crashwho.sellHopper.Utils.Files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.crashwho.sellHopper.SellHopper;

import java.io.File;

public class MessagesManager {

    private final SellHopper sellHopper;

    private final File file;
    private FileConfiguration data;

    public MessagesManager(SellHopper sellHopper) {
        this.sellHopper = sellHopper;
        file = new File(sellHopper.getDataFolder(), "messages.yml");
        setupData();

    }

    public void reloadMessages() {
        setupData();
    }

    private void setupData() {

        if (!file.exists()) {
            sellHopper.saveResource("messages.yml", false);
        }
        data = YamlConfiguration.loadConfiguration(file);

    }

    public FileConfiguration getData() {
        return data;
    }

}
