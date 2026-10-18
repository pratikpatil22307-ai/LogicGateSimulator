package generator;

import circuit.Circuit;
import gates.Gate;
import gates.InputGate;
import gates.NotGate;

/** Builds simple chains of NOT gates. Handy for studying how engines handle gate order. */
public final class ChainGenerator {

    private ChainGenerator() { }

    /**
     * One input (IN) feeding a chain of 'length' NOT gates (N1..N<length>). The last NOT is the output.
     * If reverseOrder is true, the gates are added to the circuit last-to-first with IN at the very end,
     * which is the worst order for an engine that sweeps gates in the order they were added.
     */
    public static Circuit notChain(int length, boolean reverseOrder) {
        if (length < 1) {
            throw new IllegalArgumentException("length must be at least 1");
        }
        Circuit c = new Circuit();
        InputGate in = new InputGate("IN");
        Gate[] chain = new Gate[length];
        for (int i = 0; i < length; i++) {
            chain[i] = new NotGate("N" + (i + 1));
        }

        if (reverseOrder) {
            for (int i = length - 1; i >= 0; i--) {
                c.add(chain[i]);
            }
            c.add(in);
        } else {
            c.add(in);
            for (int i = 0; i < length; i++) {
                c.add(chain[i]);
            }
        }

        c.connect(in, chain[0]);
        for (int i = 1; i < length; i++) {
            c.connect(chain[i - 1], chain[i]);
        }
        c.markOutput(chain[length - 1]);
        return c;
    }
}