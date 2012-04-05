
package net.kitecraft.tyrotoxism.gates.listeners;

import java.io.IOException;

import net.kitecraft.tyrotoxism.gates.Gate;
import net.kitecraft.tyrotoxism.gates.Gates;
import net.kitecraft.tyrotoxism.gates.RedstoneState;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ApplyListener implements Listener {

    private Gates plugin;

    public ApplyListener(Gates plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || !event.getAction().equals(Action.LEFT_CLICK_BLOCK) || !(event.getClickedBlock().getState() instanceof Sign)) { return; }

        Gate gate = this.plugin.getGate((Sign) event.getClickedBlock().getState());

        if (gate != null) {
            Player player = event.getPlayer();

            if (!this.plugin.getApplies().containsKey(player)) { return; }

            this.plugin.getGatesConfig().remove(gate);

            for (Integer line : this.plugin.getApplies().get(player).keySet()) {
                String value = this.plugin.getApplies().get(player).get(line);

                switch (line) {
                    case 1: {
                        gate.setOwner(this.plugin.getServer().getOfflinePlayer(value));
                        break;
                    }
                    case 2: {
                        gate.setGroup(value);
                        break;
                    }
                    case 3: {
                        gate.setRedstoneState(RedstoneState.getByState(value));
                        break;
                    }
                }
            }

            this.plugin.getGatesConfig().add(gate);
            this.plugin.getApplies().remove(player);

            gate.updateSign();
            event.setCancelled(true);

            try {
                this.plugin.getConfig().save(this.plugin.getConfigFile());
            } catch (IOException e) {
                e.printStackTrace();
            }

            player.sendMessage("§aGate settings applied.");
        }
    }
}
