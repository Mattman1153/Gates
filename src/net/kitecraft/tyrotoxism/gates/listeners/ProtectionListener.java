
package net.kitecraft.tyrotoxism.gates.listeners;

import net.kitecraft.tyrotoxism.gates.Gate;
import net.kitecraft.tyrotoxism.gates.Gates;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class ProtectionListener implements Listener {

    private Gates plugin;

    public ProtectionListener(Gates plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) { return; }

        event.setCancelled(!this.plugin.canBuild(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) { return; }

        Block block = event.getBlock();

        if (!this.plugin.canBuild(block)) {
            event.setCancelled(true);
            return;
        }

        for (Gate gate : this.plugin.getGates()) {
            Block attached = block.getRelative(((org.bukkit.material.Sign) gate.getSign().getData()).getAttachedFace().getOppositeFace());

            if (attached.getState() instanceof Sign) {
                Gate gate1 = this.plugin.getGate((Sign) attached.getState());

                if ((gate1 != null) && gate.equals(gate1)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
