
package net.kitecraft.tyrotoxism.gates.listeners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.kitecraft.tyrotoxism.gates.Gate;
import net.kitecraft.tyrotoxism.gates.Gates;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class GateListener implements Listener {

    private Gates plugin;

    public GateListener(Gates plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !(event.getClickedBlock().getState() instanceof Sign)) { return; }

        Player player = event.getPlayer();
        Sign sign = (Sign) event.getClickedBlock().getState();
        Gate gate = this.plugin.getGate(sign);

        event.setCancelled(true);

        if (gate != null) {
            boolean permission = false;

            if ((player.equals(gate.getOwner()) && player.hasPermission("gates.use.self")) || (!player.equals(gate.getOwner()) && player.hasPermission("gates.use.others"))) {
                permission = true;
            } else if (player.hasPermission("gates.use.group.*") || player.hasPermission(String.format("gates.use.group.%s", gate.getGroup()))) {
                permission = true;
            } else if (player.hasPermission("gates.use.player.*") || player.hasPermission(String.format("gates.use.player.%s", gate.getOwner().getName()))) {
                permission = true;
            }

            if (!permission) {
                player.sendMessage("§cYou can't use that gate.");
                return;
            }

            gate.toggle();
        } else {
            boolean isGate = false;

            for (String line : sign.getLines()) {
                if (line.equalsIgnoreCase("[Gate]")) {
                    isGate = true;
                    break;
                }
            }

            if (!isGate) {
                event.setCancelled(false);
                return;
            }

            if (!player.hasPermission("gates.create")) {
                player.sendMessage("§cYou can't create a gate.");
                return;
            }

            Gate gate1 = new Gate(this.plugin, sign, player, this.plugin.getConfig().getString("config.default.group"), this.plugin.getConfig().getString("config.default.redstone").toUpperCase());

            this.plugin.getGates().add(gate1);
            this.plugin.getGatesConfig().add(gate1);

            try {
                this.plugin.getConfig().save(this.plugin.getConfigFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled() || !(event.getBlock().getState() instanceof Sign)) { return; }

        Player player = event.getPlayer();
        Gate gate = this.plugin.getGate((Sign) event.getBlock().getState());

        if (gate != null) {
            boolean permission = false;

            if ((player.equals(gate.getOwner()) && player.hasPermission("gates.destroy.self")) || (!player.equals(gate.getOwner()) && player.hasPermission("gates.destroy.others"))) {
                permission = true;
            } else if (player.hasPermission("gates.destroy.group.*") || player.hasPermission(String.format("gates.destroy.group.%s", gate.getGroup()))) {
                permission = true;
            } else if (player.hasPermission("gates.destroy.player.*") || player.hasPermission(String.format("gates.destroy.player.%s", gate.getOwner().getName()))) {
                permission = true;
            }

            if (!permission) {
                player.sendMessage("§cYou can't destroy that gate.");
                gate.updateSign();
                event.setCancelled(true);
                return;
            }

            if (gate.isTaskRuning()) {
                gate.updateSign();
                event.setCancelled(true);
                return;
            }

            this.plugin.getGates().remove(gate);
            this.plugin.getGatesConfig().remove(gate);

            if (!gate.isOpen()) {
                List<Block> blocks = new ArrayList<Block>();

                for (Block block1 : gate.getBlocks()) {
                    if (block1.getRelative(BlockFace.UP).getType().equals(gate.getMaterial())) {
                        blocks.add(block1);
                    }
                }

                for (Block block1 : blocks) {
                    block1.setType(Material.AIR);
                }
            }

            try {
                this.plugin.getConfig().save(this.plugin.getConfigFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
