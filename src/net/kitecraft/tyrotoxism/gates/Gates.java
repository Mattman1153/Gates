
package net.kitecraft.tyrotoxism.gates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Gates extends JavaPlugin implements Listener {

    private List<Gate> gates;
    private File file;
    private Material[] materials = new Material[] { Material.FENCE, Material.NETHER_FENCE, Material.IRON_FENCE, Material.THIN_GLASS };
    private List<Material> meterialList = new ArrayList<Material>();

    @Override
    public void onEnable() {
        this.file = new File(this.getDataFolder(), "gates.yml");

        this.load();

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info(this + " is now enabled");
    }

    @Override
    public void onDisable() {
        this.getLogger().info(this + " is now disable");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) { return false; }

        if (args[0].equals("reload")) {
            if (!sender.hasPermission("gates.command.reload")) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }

            this.load();

            sender.sendMessage("§a[" + this.getDescription().getName() + "] Reload complete.");
        } else if (args[0].equals("version")) {
            if (!sender.hasPermission("gates.command.version")) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }

            sender.sendMessage((this.isEnabled() ? "§a" : "§c") + this);
        } else {
            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !(event.getClickedBlock().getState() instanceof Sign)) { return; }

        Sign sign = (Sign) event.getClickedBlock().getState();
        Player player = event.getPlayer();
        boolean isGate = false;

        for (String line : sign.getLines()) {
            if (line.equalsIgnoreCase("[Gate]")) {
                isGate = true;
                break;
            }
        }

        if (!isGate) { return; }

        Gate gate = this.getGate(sign);

        event.setCancelled(true);

        if (gate != null) {
            gate.updateSign();

            if (player != null) {
                if (player.equals(gate.getOwner())) {
                    if (!player.hasPermission("gates.use.self")) {
                        player.sendMessage("§cYou can't use that gate.");
                        return;
                    }
                } else {
                    if (!player.hasPermission("gates.use.others")) {
                        player.sendMessage("§cYou can't use that gate.");
                        return;
                    }
                }
            }

            if (gate.isTaskRuning()) { return; }

            if (this.getConfig().getBoolean("config.gate.find-blocks-on-use", false)) {
                gate.findBlocks();
            }

            if (gate.isReal()) {
                gate.setTask(this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new GateTimer(gate), this.getConfig().getInt("config.gate.delay", 16), this.getConfig().getInt("config.gate.ticks", 8)));
            }
        } else {
            if (!player.hasPermission("gates.create")) {
                player.sendMessage("§cYou don't have permission to create a gate.");
                return;
            }

            this.gates.add(new Gate(this, sign, player));

            List<String> gates = this.getConfig().getStringList("gates");
            gates.add(String.format("%s-%s-%s-%s-%s", sign.getWorld().getName(), Integer.toString(sign.getX()), Integer.toString(sign.getY()), Integer.toString(sign.getZ()), player.getName()));
            this.getConfig().set("gates", gates);

            try {
                this.getConfig().save(this.file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) { return; }

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!this.canBuild(block)) {
            event.setCancelled(true);
        } else if ((block.getState() instanceof Sign)) {
            Sign sign = (Sign) block.getState();
            Gate gate = this.getGate(sign);

            if (gate != null) {
                if (gate.getOwner().equals(player)) {
                    if (!player.hasPermission("gates.destroy.self")) {
                        player.sendMessage("§cYou can't destroy that gate.");
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!player.hasPermission("gates.destroy.others")) {
                        player.sendMessage("§cYou can't destroy that gate.");
                        event.setCancelled(true);
                        return;
                    }
                }

                if (gate.isTaskRuning()) {
                    event.setCancelled(true);
                    return;
                }

                this.gates.remove(gate);

                List<String> gates = this.getConfig().getStringList("gates");
                gates.remove(String.format("%s-%s-%s-%s-%s", sign.getWorld().getName(), Integer.toString(sign.getX()), Integer.toString(sign.getY()), Integer.toString(sign.getZ()), player.getName()));
                this.getConfig().set("gates", gates);

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
                    this.getConfig().save(this.file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            for (Gate gate : this.gates) {
                Block attached = block.getRelative(((org.bukkit.material.Sign) gate.getSign().getData()).getAttachedFace().getOppositeFace());

                if (attached.getState() instanceof Sign) {
                    Gate gate1 = this.getGate((Sign) attached.getState());

                    if ((gate1 != null) && gate.equals(gate1)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled() && !this.canBuild(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    public List<Gate> getGates() {
        return this.gates;
    }

    public Material[] getMaterials() {
        return this.materials;
    }

    public List<Material> getMaterialList() {
        if (this.meterialList.isEmpty()) {
            for (Material material : this.materials) {
                this.meterialList.add(material);
            }
        }

        return this.meterialList;
    }

    public Gate getGate(Sign sign) {
        for (Gate gate : this.gates) {
            if (gate.getSign().equals(sign)) { return gate; }
        }

        return null;
    }

    public boolean canBuild(Block block) {
        for (Gate gate : this.gates) {
            if (gate.getBlocks().contains(block)) { return false; }
        }

        return true;
    }

    private void load() {
        if (!this.file.exists()) {
            try {
                this.file.getParentFile().mkdirs();
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.getConfig().set("config.gate.delay", 16);
            this.getConfig().set("config.gate.ticks", 8);
            this.getConfig().set("config.gate.force-one-sign", true);
            this.getConfig().set("config.gate.find-blocks-on-use", false);

            try {
                this.getConfig().save(this.file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            this.getConfig().load(this.file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        this.gates = new ArrayList<Gate>();

        List<String> gates = this.getConfig().getStringList("gates");

        if (!gates.isEmpty()) {
            for (String gate : gates) {
                String[] split = gate.split("-");
                Block block = this.getServer().getWorld(split[0]).getBlockAt(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));

                if (block.getState() instanceof Sign) {
                    this.gates.add(new Gate(this, (Sign) block.getState(), this.getServer().getOfflinePlayer(split[4])));
                }
            }
        }
    }
}
