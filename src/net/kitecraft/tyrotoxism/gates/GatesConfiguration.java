
package net.kitecraft.tyrotoxism.gates;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

public class GatesConfiguration {

    private FileConfiguration config;

    public GatesConfiguration(FileConfiguration config) {
        this.config = config;
    }

    public int getInt(String path, String group) {
        String groupPath = path.replace("global", String.format("group.%s", group));

        if (this.config.contains(groupPath)) { return this.config.getInt(groupPath); }
        return this.config.getInt(path);
    }

    public boolean getBoolean(String path, String group) {
        String groupPath = path.replace("global", String.format("group.%s", group));

        if (this.config.contains(groupPath)) { return this.config.getBoolean(groupPath); }
        return this.config.getBoolean(path);
    }

    public void remove(Gate gate) {
        List<String> gates = this.config.getStringList("gates");
        gates.remove(gate.getConfigString());
        this.config.set("gates", gates);
    }

    public void add(Gate gate) {
        List<String> gates = this.config.getStringList("gates");
        gates.add(gate.getConfigString());
        this.config.set("gates", gates);
    }
}
