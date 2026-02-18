package org.crashwho.sellHopper.Events;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.GameMode;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;
import org.crashwho.crashShop.CrashShop;
import org.crashwho.sellHopper.SellHopper;
import org.crashwho.sellHopper.Utils.ChatFormat;
import org.crashwho.sellHopper.Utils.Inventory.GuiManager;
import org.crashwho.sellHopper.Utils.Inventory.HopperInventory;
import org.crashwho.sellHopper.Utils.Items.HopperItem;

public class BlockEvent implements Listener {

    final SellHopper sellHopper;

    public BlockEvent(SellHopper sellHopper) {
        this.sellHopper = sellHopper;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        HopperItem hopperItem = new HopperItem(sellHopper);

        if (e.getItemInHand().isSimilar(hopperItem.hopperItem())) {
            if (e.getBlockPlaced().getState() instanceof Hopper hopper) {
                hopper.getPersistentDataContainer().set(sellHopper.getKey(), PersistentDataType.DOUBLE, 0d);
                hopper.update();
            }
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            assert e.getClickedBlock() != null;
            if (e.getClickedBlock().getState() instanceof Hopper hopper) {
                if (hopper.getPersistentDataContainer().has(sellHopper.getKey(), PersistentDataType.DOUBLE)) {

                    if (e.getPlayer().isSneaking()) {
                        if (e.getItem() != null && e.getItem().getType().isBlock()) return;
                        e.setCancelled(true);
                        return;
                    }

                    e.setCancelled(true);
                    HopperInventory hopperInventory = new HopperInventory(sellHopper, hopper);
                    GuiManager.register(hopper.getLocation(), hopperInventory);
                    e.getPlayer().openInventory(hopperInventory.getInventory());
                }
            }
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getState() instanceof Hopper hopper) {
            if (hopper.getPersistentDataContainer().has(sellHopper.getKey(), PersistentDataType.DOUBLE)) {
                double value = hopper.getPersistentDataContainer().get(sellHopper.getKey(), PersistentDataType.DOUBLE);

                if (value > 0) {
                    CrashShop.getEconomy().depositPlayer(e.getPlayer(), value);
                    e.getPlayer().sendMessage(ChatFormat.prefixFormat(sellHopper.getMessages().getString("messages.deposit"),
                            Placeholder.unparsed("money", CrashShop.getEconomy().format(value))));
                }

                if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;

                e.setDropItems(false);
                hopper.getWorld().dropItemNaturally(hopper.getLocation(), new HopperItem(sellHopper).hopperItem());


            }

        }


    }

}
