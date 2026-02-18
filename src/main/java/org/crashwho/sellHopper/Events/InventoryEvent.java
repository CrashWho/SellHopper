package org.crashwho.sellHopper.Events;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Hopper;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;
import org.crashwho.crashShop.CrashShop;
import org.crashwho.crashShop.api.ShopItem;
import org.crashwho.sellHopper.SellHopper;
import org.crashwho.sellHopper.Utils.ChatFormat;
import org.crashwho.sellHopper.Utils.Inventory.GuiManager;
import org.crashwho.sellHopper.Utils.Inventory.HopperInventory;

import java.util.*;

public class InventoryEvent implements Listener {

    private final SellHopper sellHopper;

    public InventoryEvent(SellHopper sellHopper) {
        this.sellHopper = sellHopper;
    }

    @EventHandler
    public void onInventoryPickup(InventoryPickupItemEvent e) {
        if (e.getInventory().getType() != InventoryType.HOPPER) return;

        if (!(e.getInventory().getHolder() instanceof Hopper hopper)) return;

        if (!hopper.getPersistentDataContainer().has(sellHopper.getKey(), PersistentDataType.DOUBLE)) return;

        e.setCancelled(true);
        ItemStack item = e.getItem().getItemStack();
        ShopItem shopItem = sellHopper.getShopCache().get(item.getType());

        if (shopItem != null) {
            e.getItem().remove();
            inventoryPickupLogic(shopItem, item.getAmount(), hopper);
            return;
        }

        checkShulkerBox(item, e, hopper);

    }

    private void checkShulkerBox(ItemStack item, InventoryPickupItemEvent e, Hopper hopper) {

        if (Tag.SHULKER_BOXES.isTagged(item.getType())) {

            if (item.getItemMeta() instanceof BlockStateMeta bsm) {
                if (bsm.getBlockState() instanceof ShulkerBox shulkerBox) {

                    double totalValue = 0;
                    boolean inventoryChange = false;

                    for (int i = 0; i < shulkerBox.getInventory().getContents().length; i++) {
                        ItemStack content = shulkerBox.getInventory().getContents()[i];
                        if (content != null  && content.getType() != Material.AIR) {
                            ShopItem shopItm =  sellHopper.getShopCache().get(content.getType());
                            if (shopItm != null && shopItm.canSell()) {
                                totalValue += shopItm.getSellPrice() * content.getAmount();
                                shulkerBox.getInventory().setItem(i, null);
                                inventoryChange = true;
                            }
                        }

                    }

                    if (inventoryChange) {

                        addMoneyToHopper(hopper, totalValue);
                        shulkerBox.update();
                        bsm.setBlockState(shulkerBox);
                        item.setItemMeta(bsm);

                        e.getItem().setItemStack(item);
                    }
                }
            }
        }

    }

    private void addMoneyToHopper(Hopper hopper, double amountToAdd) {
        double current = hopper.getPersistentDataContainer()
                .getOrDefault(sellHopper.getKey(), PersistentDataType.DOUBLE, 0.0);

        hopper.getPersistentDataContainer().set(sellHopper.getKey(), PersistentDataType.DOUBLE, current + amountToAdd);
        hopper.update();
        GuiManager.updateGuisAt(hopper.getLocation());
    }

    @EventHandler
    public void onItemMove(InventoryMoveItemEvent e) {

        if (e.getSource().getHolder() instanceof Hopper hopper)
            if (hopper.getPersistentDataContainer().has(sellHopper.getKey(), PersistentDataType.DOUBLE)) return;

        if (e.getDestination().getHolder() instanceof Hopper hopper) {
            if (hopper.getPersistentDataContainer().has(sellHopper.getKey(), PersistentDataType.DOUBLE)) {

                ShopItem shopItem = sellHopper.getShopCache().get(e.getItem().getType());
                if (shopItem != null && shopItem.canSell()) {
                    inventoryPickupLogic(shopItem, e.getItem().getAmount(), hopper);
                    e.setItem(new ItemStack(Material.AIR));
                } else e.setCancelled(true);

            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if (e.getInventory().getHolder() instanceof HopperInventory hopperInventory) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null) return;

            if (e.getCurrentItem().isSimilar(hopperInventory.getItem())) {


                double money = hopperInventory.getHopper().getPersistentDataContainer().getOrDefault(sellHopper.getKey(), PersistentDataType.DOUBLE, 0d);
                depositPlayer((Player) e.getWhoClicked(), money, hopperInventory.getHopper());
                GuiManager.updateGuisAt(hopperInventory.getHopper().getLocation());
            }

        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof HopperInventory hopperInventory) {
            GuiManager.unregister(hopperInventory.getHopper().getLocation(), hopperInventory);
        }
    }

    private void depositPlayer(Player player, double money, TileState hopper) {
        if (money > 0) {
            CrashShop.getEconomy().depositPlayer(player, money);
            player.sendMessage(ChatFormat.prefixFormat(sellHopper.getMessages().getString("messages.deposit"),
                    Placeholder.unparsed("money", CrashShop.getEconomy().format(money))));

            hopper.getPersistentDataContainer().set(sellHopper.getKey(), PersistentDataType.DOUBLE, 0d);
            hopper.update();
        }
    }

    private void inventoryPickupLogic(ShopItem item, int amount, TileState hopper) {
        double sell = item.getSellPrice() * amount;
        double value = hopper.getPersistentDataContainer().getOrDefault(sellHopper.getKey(), PersistentDataType.DOUBLE, 0d);
        value += sell;

        hopper.getPersistentDataContainer().set(sellHopper.getKey(), PersistentDataType.DOUBLE, value);
        hopper.update(true);
        GuiManager.updateGuisAt(hopper.getLocation());
    }

}
