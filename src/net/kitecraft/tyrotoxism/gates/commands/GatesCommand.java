
package net.kitecraft.tyrotoxism.gates.commands;

import java.util.HashMap;

import net.kitecraft.tyrotoxism.gates.Gates;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GatesCommand implements CommandExecutor {

    private Gates plugin;

    public GatesCommand(Gates plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!((args.length == 1) || (args.length == 2))) { return false; }

        if ((args.length == 1) && args[0].equals("reload")) {
            if (!sender.hasPermission("gates.command.reload")) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }

            this.plugin.load();

            sender.sendMessage("§a[" + this.plugin.getDescription().getName() + "] Reload complete.");
        } else if ((args.length == 1) && args[0].equals("version")) {
            if (!sender.hasPermission("gates.command.version")) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }

            sender.sendMessage((this.plugin.isEnabled() ? "§a" : "§c") + this);
        } else if ((args.length == 2) && args[0].equals("setowner")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis is a in-game command only.");
                return true;
            }

            Player player = (Player) sender;
            HashMap<Integer, String> applies;

            if (this.plugin.getApplies().containsKey(player)) {
                applies = this.plugin.getApplies().get(player);
            } else {
                applies = new HashMap<Integer, String>();
            }

            applies.put(1, args[1]);
            this.plugin.getApplies().put(player, applies);

            sender.sendMessage("§eReady to apply new owner.");
        } else if ((args.length == 2) && args[0].equals("setgroup")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis is a in-game command only.");
                return true;
            }

            Player player = (Player) sender;
            HashMap<Integer, String> applies;

            if (this.plugin.getApplies().containsKey(player)) {
                applies = this.plugin.getApplies().get(player);
            } else {
                applies = new HashMap<Integer, String>();
            }

            applies.put(2, args[1]);
            this.plugin.getApplies().put(player, applies);

            sender.sendMessage("§eReady to apply new owner.");
        } else if ((args.length == 2) && args[0].equals("setredstone")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis is a in-game command only.");
                return true;
            }

            if (!(args[1].equalsIgnoreCase("ON") || args[1].equalsIgnoreCase("OFF") || args[1].equalsIgnoreCase("TOGGLE"))) { return false; }

            Player player = (Player) sender;
            HashMap<Integer, String> applies;

            if (this.plugin.getApplies().containsKey(player)) {
                applies = this.plugin.getApplies().get(player);
            } else {
                applies = new HashMap<Integer, String>();
            }

            applies.put(3, args[1].toUpperCase());
            this.plugin.getApplies().put(player, applies);

            sender.sendMessage("§eReady to apply new redstone state.");
        } else {
            return false;
        }

        return true;
    }
}
