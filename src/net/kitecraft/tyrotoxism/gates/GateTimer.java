
package net.kitecraft.tyrotoxism.gates;

public class GateTimer implements Runnable {

    private Gate gate;

    public GateTimer(Gate gate) {
        this.gate = gate;
    }

    @Override
    public void run() {
        if (this.gate.isTaskRuning()) {
            this.gate.toggle();
        } else {
            try {
                this.finalize();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
