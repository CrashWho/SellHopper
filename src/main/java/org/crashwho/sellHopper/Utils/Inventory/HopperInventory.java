package org.crashwho.sellHopper.Utils.Inventory;

import org.bukkit.block.TileState;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.crashwho.sellHopper.SellHopper;
import org.crashwho.sellHopper.Utils.Items.HopperItem;
import org.jetbrains.annotations.NotNull;

public class HopperInventory implements InventoryHolder {

    private final SellHopper sellHopper;
    private final Inventory inventory;
    private final TileState hopper;

    private ItemStack item;

    public HopperInventory(SellHopper sellHopper, TileState hopper) {
        this.sellHopper = sellHopper;
        this.inventory = sellHopper.getServer().createInventory(this, InventoryType.HOPPER);
        this.hopper = hopper;
        setup();
    }

    public void update() {
        setup();
    }

    private void setup() {

        TileState newState = (TileState) hopper.getBlock().getState();

        int slot = sellHopper.getConfig().getInt("items.inventory.slot");
        if (hopper.getPersistentDataContainer().has(sellHopper.getKey(), PersistentDataType.DOUBLE)) {
            double value = newState.getPersistentDataContainer().get(sellHopper.getKey(), PersistentDataType.DOUBLE);
            this.item = new HopperItem(sellHopper).inventoryItem(value);
            this.inventory.setItem(slot, item);
        }
    }

    public TileState getHopper() {
        return hopper;
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
