package com.LertusBankNotes;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class Withdraw implements CommandExecutor {

    private Main plugin;

    public Withdraw(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can withdraw bank notes!");
        } else if (!sender.hasPermission("Notes.withdraw")) {
            sender.sendMessage((ChatColor.RED + "No Permission!"));
        } else if (args.length == 0) {
            return false;
        } else {
            Player player = (Player) sender;
            double amount;

            try {
                amount = args[0].equalsIgnoreCase("all")
                        ? plugin.getEconomy().getBalance(player) : Double.parseDouble(args[0]);
            } catch (NumberFormatException invalidNumber) {
                player.sendMessage((ChatColor.RED + "Invalid Number"));
                return true;
            }



            if (Double.compare(plugin.getEconomy().getBalance(player), amount) < 0) {
                player.sendMessage(plugin.getConfig().getString(ChatColor.RED + "Not Enough Money!"));
            } else if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage((ChatColor.RED + "Inventory Full"));
            } else {
                ItemStack banknote = plugin.createBanknote(player.getName(), amount);
                EconomyResponse response = plugin.getEconomy().withdrawPlayer(player, amount);

                if (response == null || !response.transactionSuccess()) {
                    player.sendMessage(ChatColor.RED + "There was an error processing your transaction");
                    return true;
                }

                player.getInventory().addItem(banknote);
                player.sendMessage(plugin.getColor(plugin.getConfig().getString("Withdraw-msg")).replace("{amount}", plugin.formatDouble(amount)));
            }
        }

        return true;
    }
}
