package com.LertusBankNotes;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private Main plugin;

    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerGetNote(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!event.getPlayer().hasPermission("Notes.deposit")) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        if (item == null || !plugin.isNote(item)) {
            return;
        }

        double amount = plugin.getNoteAmount(item);

        if (Double.compare(amount, 0) < 0) {
            return;
        }

        EconomyResponse response = plugin.getEconomy().depositPlayer(player, amount);
        if (response == null || !response.transactionSuccess()) {
            player.sendMessage(ChatColor.RED + "There was an error processing your transaction");
            return;
        }

        player.sendMessage(plugin.getColor("Redeemed").replace("{amount}", plugin.formatDouble(amount)));

        if (item.getAmount() <= 1) {
            event.getPlayer().getInventory().removeItem(item);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }
}
