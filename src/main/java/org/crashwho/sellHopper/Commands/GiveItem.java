package org.crashwho.sellHopper.Commands;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.crashwho.sellHopper.SellHopper;
import org.crashwho.sellHopper.Utils.ChatFormat;
import org.crashwho.sellHopper.Utils.Items.HopperItem;

import java.util.List;
import java.util.stream.Collectors;


public class GiveItem {

    private final SellHopper sellHopper;

    public GiveItem(SellHopper sellHopper) {
        this.sellHopper = sellHopper;
    }

    public LiteralCommandNode<CommandSourceStack> hopperCommand() {
        return Commands.literal("sellhopper")
                .requires(sender -> sender.getSender().hasPermission("sellhopper.use"))

                .then(Commands.literal("reload")
                        .requires(sender -> sender.getSender().hasPermission("sellhopper.reload"))
                        .executes(this::reloadCommand))

                .then(Commands.literal("give")
                        .then(Commands.argument("player", ArgumentTypes.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 2304))
                                        .executes(this::executeGiveCommand))))

                .build();
    }

    private int reloadCommand(CommandContext<CommandSourceStack> ctx) {
        sellHopper.reloadFiles();
        ctx.getSource().getSender().sendMessage(ChatFormat.prefixFormat(sellHopper.getMessages().getString("messages.reload")));
        return Command.SINGLE_SUCCESS;
    }

    private int executeGiveCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
        List<Player> targets = resolver.resolve(ctx.getSource());
        int amount = ctx.getArgument("amount", Integer.class);
        ItemStack hopperItem = new HopperItem(sellHopper).hopperItem();
        hopperItem.setAmount(amount);

        for (Player target : targets) {
            giveOrDrop(target, hopperItem);
        }

        String targetName = targets.stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "));

        ctx.getSource().getSender().sendMessage(ChatFormat.prefixFormat(sellHopper.getMessages().getString("messages.give"),
                Formatter.number("amount", amount),
                Placeholder.unparsed("player", targetName)));

        return Command.SINGLE_SUCCESS;
    }

    private void giveOrDrop(Player player, ItemStack item) {
        var items = player.getInventory().addItem(item);
        if (!items.isEmpty())
            items.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
    }

}
