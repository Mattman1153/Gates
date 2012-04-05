
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
    private String group;
    private RedstoneState redstone;

    public Gate(Gates plugin, Sign sign, OfflinePlayer owner, String group, String redstone) {
        this.plugin = plugin;
        this.sign = sign;
        this.owner = owner;
        this.open = false;
        this.openCheck = false;
        this.runingTask = false;
        this.task = 0;
        this.group = group;
        this.redstone = RedstoneState.getByState(redstone);

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

    public String getGroup() {
        return this.group;
    }

    public RedstoneState getRedstoneState() {
        return this.redstone;
    }

    public String getConfigString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s", this.sign.getWorld().getName(), Integer.toString(this.sign.getX()), Integer.toString(this.sign.getY()), Integer.toString(this.sign.getZ()), this.owner.getName(), this.group, this.redstone.getState());
    }

    public void setOwner(OfflinePlayer owner) {
        this.owner = owner;
    }

    public void setRedstoneState(RedstoneState state) {
        this.redstone = state;
    }

    public void setGroup(String group) {
        this.group = group;
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

    public void updateSign() {
        this.sign.setLine(0, "[Gate]");
        this.sign.setLine(1, this.owner.getName());
        this.sign.setLine(2, this.group);
        this.sign.setLine(3, this.redstone.getState());
        this.sign.update();
    }

    public void toggle() {
        if (this.runingTask) { return; }

        if (this.plugin.getGatesConfig().getBoolean("config.global.find-blocks-on-use", this.group)) {
            this.findBlocks();
        }

        if (this.isReal()) {
            int delay = this.plugin.getGatesConfig().getInt("config.global.delay", this.group);
            int ticks = this.plugin.getGatesConfig().getInt("config.global.ticks", this.group);

            if (ticks == 0) {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        if (Gate.this.isOpen()) {
                            for (Block block : Gate.this.blocks) {
                                block.setType(Gate.this.material);
                            }
                        } else {
                            List<Block> blocks = new ArrayList<Block>();

                            for (Block block : Gate.this.blocks) {
                                if (block.getRelative(BlockFace.UP).getType().equals(Gate.this.material)) {
                                    blocks.add(block);
                                }
                            }

                            for (Block block : blocks) {
                                block.setType(Material.AIR);
                            }
                        }

                        Gate.this.open = !Gate.this.open;
                    }
                };

                this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, task, delay);
            } else {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        boolean finished = true;
                        List<Block> blocks = new ArrayList<Block>();

                        for (Block block : Gate.this.blocks) {
                            if (((Gate.this.isOpen() && !block.getType().equals(Gate.this.material)) || (!Gate.this.isOpen() && block.getType().equals(Gate.this.material))) && block.getRelative(BlockFace.UP).getType().equals(Gate.this.material)) {
                                blocks.add(block);
                            }
                        }

                        int y = Gate.this.isOpen() ? 0 : Gate.this.blocks.get(0).getWorld().getMaxHeight();

                        for (Block block : blocks) {
                            if (Gate.this.isOpen() ? block.getY() > y : block.getY() < y) {
                                y = block.getY();
                            }
                        }

                        for (Block block : blocks.toArray(new Block[blocks.size()])) {
                            if (block.getY() != y) {
                                blocks.remove(block);
                            }
                        }

                        if (!blocks.isEmpty()) {
                            if (Gate.this.isOpen()) {
                                for (Block block : blocks) {
                                    block.setType(Gate.this.material);
                                }

                                for (Block block : Gate.this.blocks) {
                                    if (block.getType().equals(Material.AIR)) {
                                        finished = false;
                                    }
                                }
                            } else {
                                for (Block block : blocks) {
                                    block.setType(Material.AIR);
                                }

                                for (Block block : Gate.this.blocks) {
                                    if (block.getType().equals(Gate.this.material) && block.getRelative(BlockFace.UP).getType().equals(Gate.this.material)) {
                                        finished = false;
                                    }
                                }
                            }
                        }

                        if (finished) {
                            Gate.this.open = !Gate.this.open;
                            Gate.this.plugin.getServer().getScheduler().cancelTask(Gate.this.task);
                            Gate.this.runingTask = false;
                        }
                    }
                };

                this.task = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, task, delay, ticks);
                this.runingTask = true;
            }

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

                    if (this.plugin.getGatesConfig().getBoolean("config.global.force-one-sign", this.group)) {
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
