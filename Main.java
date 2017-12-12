package com.LertusBankNotes;

import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main extends JavaPlugin {

    private ItemStack base;

    private List<String> baseLore;

    private Economy economy;

    private final Pattern MONEY_PATTERN = Pattern.compile("((([1-9]\\d{0,2}(,\\d{3})*)|(([1-9]\\d*)?\\d))(\\.?\\d?\\d?)?$)");

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getCommand("withdraw").setExecutor(new Withdraw(this));
        getCommand("banknotes").setExecutor(new NoteCommand(this));

        getServer().getScheduler().runTask(this, new Runnable() {
            @Override
            public void run() {
                RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);

                if (provider == null) {
                    getServer().getPluginManager().disablePlugin(Main.this);
                } else {
                    economy = provider.getProvider();
                }
            }
        });

        reload();
    }

    public Economy getEconomy() {
        return economy;
    }

    public String formatDouble(double value) {
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

        int max = 0;
        int min = 0;

        nf.setMaximumFractionDigits(max);
        nf.setMinimumFractionDigits(min);
        return nf.format(value);
    }

    public String colorMessage(String message) {
        if (message == null) {
            return message;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getColor(String path) {

        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(path));
    }


    public void reload() {
        reloadConfig();

        base = new ItemStack(Material.PAPER);
        ItemMeta meta = base.getItemMeta();
        meta.setDisplayName(colorMessage(getConfig().getString("NoteName", "Banknote")));
        base.setItemMeta(meta);

        baseLore = getConfig().getStringList("Lore");
    }

    public ItemStack createBanknote(String creatorName, double amount) {
        if (creatorName.equals("CONSOLE")) {
            creatorName = "CONSOLE";
        }
        List<String> formatLore = new ArrayList<String>();

        for (String baseLore : this.baseLore) {
            formatLore.add(colorMessage(baseLore.replace("{amount}", formatDouble(amount)).replace("{player}", creatorName)));
        }

        ItemStack ret = base.clone();
        ItemMeta meta = ret.getItemMeta();
        meta.setLore(formatLore);
        ret.setItemMeta(meta);

        return ret;
    }


    public boolean isNote(ItemStack itemstack) {
        if (itemstack.getType() == base.getType() && itemstack.getDurability() == base.getDurability()
                && itemstack.getItemMeta().hasDisplayName() && itemstack.getItemMeta().hasLore()) {
            String display = itemstack.getItemMeta().getDisplayName();
            List<String> lore = itemstack.getItemMeta().getLore();

            return display.equals(this.getColor("NoteName")) && lore.size() == getConfig().getStringList("NoteLore").size();
        }
        return false;
    }


    public double getNoteAmount(ItemStack itemstack) {
        if (itemstack.getItemMeta().hasDisplayName() && itemstack.getItemMeta().hasLore()) {
            String display = itemstack.getItemMeta().getDisplayName();
            List<String> lore = itemstack.getItemMeta().getLore();

            if (display.equals(this.getColor("NoteName"))) {
                for (String money : lore) {
                    Matcher matcher = MONEY_PATTERN.matcher(money);

                    if (matcher.find()) {
                        String amount = matcher.group(1);
                        return Double.parseDouble(amount.replaceAll(",", ""));


                    }
                }
            }
        }
        return 0;
    }
}
