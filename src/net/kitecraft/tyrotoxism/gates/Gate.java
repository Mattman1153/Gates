
package net.kitecraft.tyrotoxism.gates;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class Gate {

    private Gates plugin;
    private Sign sign;
    private OfflinePlayer owner;
    private List<Block> blocks;
    private boolean open, openCheck, runingTask;
    private int task;
    private Material material;

    public Gate(Gates plugin, Sign sign, OfflinePlayer owner) {
        this.plugin = plugin;
        this.sign = sign;
        this.owner = owner;
        this.open = false;
        this.openCheck = false;
        this.runingTask = false;
        this.task = 0;

        this.findBlocks();
        this.updateSign();
    }

    public Sign getSign() {
        return this.sign;
    }

    public OfflinePlayer getOwner() {
        return this.owner;
    }

    public List<Block> getBlocks() {
        return this.blocks;
    }

    public boolean isReal() {
        return !this.blocks.isEmpty();
    }

    public Material getMaterial() {
        return this.material;
    }

    public boolean isOpen() {
        if (!this.openCheck) {
            this.openCheck = true;

            for (Block block : this.blocks) {
                if (block.getType().equals(Material.AIR)) {
                    this.open = true;
                    return true;
                }
            }

            this.open = false;
            return false;
        } else {
            return this.open;
        }
    }

    public boolean isTaskRuning() {
        return this.runingTask;
    }

    public void setTask(int task) {
        this.task = task;
        this.runingTask = true;
    }

    public void stopTask() {
        this.plugin.getServer().getScheduler().cancelTask(this.task);
        this.runingTask = false;
    }

    public void updateSign() {
        this.sign.setLine(0, "");
        this.sign.setLine(1, "[Gate]");
        this.sign.setLine(2, this.owner.getName());
        this.sign.setLine(3, "");

        this.sign.update();
    }

    public void toggle() {
        boolean finished = true;
        List<Block> blocks = new ArrayList<Block>();

        for (Block block : this.blocks) {
            if (((this.isOpen() && !block.getType().equals(this.material)) || (!this.isOpen() && block.getType().equals(this.material))) && block.getRelative(BlockFace.UP).getType().equals(this.material)) {
                blocks.add(block);
            }
        }

        int y = this.isOpen() ? 0 : this.blocks.get(0).getWorld().getMaxHeight();

        for (Block block : blocks) {
            if (this.isOpen() ? block.getY() > y : block.getY() < y) {
                y = block.getY();
            }
        }

        for (Block block : blocks.toArray(new Block[blocks.size()])) {
            if (block.getY() != y) {
                blocks.remove(block);
            }
        }

        if (!blocks.isEmpty()) {
            if (this.isOpen()) {
                for (Block block : blocks) {
                    block.setType(this.material);
                }

                for (Block block : this.blocks) {
                    if (block.getType().equals(Material.AIR)) {
                        finished = false;
                    }
                }
            } else {
                for (Block block : blocks) {
                    block.setType(Material.AIR);
                }

                for (Block block : this.blocks) {
                    if (block.getType().equals(this.material) && block.getRelative(BlockFace.UP).getType().equals(this.material)) {
                        finished = false;
                    }
                }
            }
        }

        if (finished) {
            this.open = !this.open;
            this.stopTask();
        }
    }

    public void findBlocks() {
        this.blocks = new ArrayList<Block>();

        boolean stop = false;

        for (int x = this.sign.getX() - 2; x < (this.sign.getX() + 3); x++) {
            for (int y = this.sign.getY() - 4; y < (this.sign.getY() + 5); y++) {
                for (int z = this.sign.getZ() - 2; z < (this.sign.getZ() + 3); z++) {
                    Block relative = this.sign.getWorld().getBlockAt(x, y, z);

                    if (!this.plugin.getMaterialList().contains(relative.getType())) {
                        continue;
                    }

                    boolean exists = false;

                    if (this.plugin.getConfig().getBoolean("config.gate.force-one-sign", true)) {
                        for (Gate gate : this.plugin.getGates()) {
                            if (gate.getBlocks().contains(relative)) {
                                exists = true;
                                break;
                            }
                        }
                    }

                    if (!exists && !this.blocks.contains(relative)) {
                        this.blocks.add(relative);
                        this.material = relative.getType();
                        this.find(relative);
                        stop = true;
                    }

                    if (stop) {
                        break;
                    }
                }

                if (stop) {
                    break;
                }
            }

            if (stop) {
                break;
            }
        }

        if (!this.blocks.isEmpty()) {
            for (Block block : this.blocks.toArray(new Block[this.blocks.size()])) {
                Block relative1 = block.getRelative(BlockFace.DOWN);

                while (relative1.getType().equals(Material.AIR) || relative1.getType().equals(this.material)) {
                    this.blocks.add(relative1);
                    relative1 = relative1.getRelative(BlockFace.DOWN);
                }
            }
        }
    }

    private void find(Block block) {
        for (int x = block.getX() - 1; x < (block.getX() + 2); x++) {
            for (int y = block.getY() - 1; y < (block.getY() + 2); y++) {
                for (int z = block.getZ() - 1; z < (block.getZ() + 2); z++) {
                    Block relative = block.getWorld().getBlockAt(x, y, z);

                    if (!this.blocks.contains(relative) && relative.getType().equals(this.material)) {
                        this.blocks.add(relative);
                        this.find(relative);
                    }
                }
            }
        }
    }
}
