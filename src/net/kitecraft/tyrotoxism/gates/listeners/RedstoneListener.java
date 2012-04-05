
package net.kitecraft.tyrotoxism.gates.listeners;

import net.kitecraft.tyrotoxism.gates.Gate;
import net.kitecraft.tyrotoxism.gates.Gates;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class RedstoneListener implements Listener {

    private Gates plugin;

    public RedstoneListener(Gates plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        Block block = event.getBlock();

        for (Gate gate : this.plugin.getGates()) {
            if (block.equals(gate.getSign().getBlock())) {
                switch (gate.getRedstoneState()) {
                    case REDSTONE_ON: {
                        if ((!gate.isOpen() && !(block.isBlockPowered() || block.isBlockIndirectlyPowered())) || (gate.isOpen() && (block.isBlockPowered() || block.isBlockIndirectlyPowered()))) {
                            gate.toggle();
                        }

                        return;
                    }
                    case REDSTONE_TOGGLE: {
                        if (block.isBlockPowered() || block.isBlockIndirectlyPowered()) {
                            gate.toggle();
                        }

                        return;
                    }
                }

                return;
            }
        }
    }
}
