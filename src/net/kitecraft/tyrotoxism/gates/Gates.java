
package net.kitecraft.tyrotoxism.gates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.kitecraft.tyrotoxism.gates.commands.GatesCommand;
import net.kitecraft.tyrotoxism.gates.listeners.ApplyListener;
import net.kitecraft.tyrotoxism.gates.listeners.GateListener;
import net.kitecraft.tyrotoxism.gates.listeners.ProtectionListener;
import net.kitecraft.tyrotoxism.gates.listeners.RedstoneListener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Gates extends JavaPlugin implements Listener {

    private List<Gate> gates;
    private File file;
    private Material[] materials = new Material[] { Material.FENCE, Material.NETHER_FENCE, Material.IRON_FENCE, Material.THIN_GLASS };
    private List<Material> meterialList = new ArrayList<Material>();
    private HashMap<Player, HashMap<Integer, String>> applies;
    private GatesConfiguration config;

    @Override
    public void onEnable() {
        this.file = new File(this.getDataFolder(), "gates.yml");

        this.load();

        this.getServer().getPluginManager().registerEvents(new ApplyListener(this), this);
        this.getServer().getPluginManager().registerEvents(new GateListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        this.getServer().getPluginManager().registerEvents(new RedstoneListener(this), this);

        this.getCommand("gates").setExecutor(new GatesCommand(this));

        this.getLogger().info(this + " is now enabled");
    }

    @Override
    public void onDisable() {
        this.getLogger().info(this + " is now disable");
    }

    public List<Gate> getGates() {
        return this.gates;
    }

    public File getConfigFile() {
        return this.file;
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

    public HashMap<Player, HashMap<Integer, String>> getApplies() {
        return this.applies;
    }

    public GatesConfiguration getGatesConfig() {
        return this.config;
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

    public void load() {
        if (!this.file.exists()) {
            try {
                this.file.getParentFile().mkdirs();
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.getConfig().set("config.default.group", "default");
            this.getConfig().set("config.default.redstone", "OFF");

            this.getConfig().set("config.global.delay", 16);
            this.getConfig().set("config.global.ticks", 8);
            this.getConfig().set("config.global.force-one-sign", true);
            this.getConfig().set("config.global.find-blocks-on-use", false);

            this.getConfig().set("config.group.default.find-blocks-on-use", true);

            this.getConfig().set("config.group.instant.delay", 0);
            this.getConfig().set("config.group.instant.ticks", 0);

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
        this.applies = new HashMap<Player, HashMap<Integer, String>>();
        this.config = new GatesConfiguration(this.getConfig());

        List<String> gates = this.getConfig().getStringList("gates");

        if (!gates.isEmpty()) {
            for (String gate : gates) {
                String[] split = gate.split(",");
                Block block = this.getServer().getWorld(split[0]).getBlockAt(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));

                if (block.getState() instanceof Sign) {
                    this.gates.add(new Gate(this, (Sign) block.getState(), this.getServer().getOfflinePlayer(split[4]), split[5], split[6]));
                }
            }
        }
    }
}
