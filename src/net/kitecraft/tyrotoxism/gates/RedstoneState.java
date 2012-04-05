
package net.kitecraft.tyrotoxism.gates;

import java.util.HashMap;

public enum RedstoneState {
    REDSTONE_ON("ON"),
    REDSTONE_OFF("OFF"),
    REDSTONE_TOGGLE("TOGGLE");

    private final String state;
    private static final HashMap<String, RedstoneState> BY_STATE = new HashMap<String, RedstoneState>();

    RedstoneState(String state) {
        this.state = state;
    }

    public String getState() {
        return this.state;
    }

    public static RedstoneState getByState(String state) {
        return RedstoneState.BY_STATE.get(state);
    }

    static {
        for (RedstoneState state : RedstoneState.values()) {
            RedstoneState.BY_STATE.put(state.getState(), state);
        }
    }
}
