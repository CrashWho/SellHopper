package org.crashwho.sellHopper.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;
import org.crashwho.sellHopper.SellHopper;

import java.io.File;

public class ChatFormat {

    private static String prefix = "";

    public static void init(SellHopper sellHopper) {
        File file = new File(sellHopper.getDataFolder(), "messages.yml");

        prefix = YamlConfiguration.loadConfiguration(file).getString("messages.prefix", "");
    }

    public static Component format(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component format(String message, TagResolver... tagResolver) {
        return MiniMessage.miniMessage().deserialize(message, tagResolver);
    }

    public static Component prefixFormat(String message) {
        return MiniMessage.miniMessage().deserialize(prefix + message);
    }

    public static Component prefixFormat(String message, TagResolver... tagresolver) {
        return MiniMessage.miniMessage().deserialize(prefix + message, tagresolver);
    }

}
