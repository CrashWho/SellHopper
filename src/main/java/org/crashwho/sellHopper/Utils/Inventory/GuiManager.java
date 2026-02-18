package org.crashwho.sellHopper.Utils.Inventory;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiManager {

    private static final Map<Location, List<HopperInventory>> activeMenus = new HashMap<>();

    public static void register(Location loc, HopperInventory gui) {
        activeMenus.computeIfAbsent(loc, k -> new ArrayList<>()).add(gui);
    }

    public static void unregister(Location loc, HopperInventory gui) {
        if (activeMenus.containsKey(loc)) {
            List<HopperInventory> list = activeMenus.get(loc);
            list.remove(gui);

            if (list.isEmpty()) {
                activeMenus.remove(loc);
            }
        }
    }


    public static void updateGuisAt(Location loc) {
        if (activeMenus.containsKey(loc)) {
            for (HopperInventory gui : activeMenus.get(loc)) {
                gui.update();
            }
        }
    }
}
