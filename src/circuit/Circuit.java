package circuit;

import exceptions.CircuitException;
import exceptions.CycleDetectedException;
import gates.Gate;
import gates.InputGate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A set of gates and the wires between them. */
public class Circuit {
    private final List<Gate> gates = new ArrayList<>();
    private final List<InputGate> inputs = new ArrayList<>();
    private final List<Gate> outputs = new ArrayList<>();
    private final Map<String, Gate> byName = new HashMap<>();

    /** Cached evaluation order. null means "not computed yet, or the circuit changed since". */
    private List<Gate> topoOrder;

    /** Adds a gate to the circuit and returns it, so you can write: InputGate a = c.add(new InputGate("A")); */
    public <T extends Gate> T add(T gate) {
        if (gate == null) {
            throw new CircuitException("gate cannot be null");
        }
        if (byName.containsKey(gate.getName())) {
            throw new CircuitException("duplicate gate name: " + gate.getName());
        }
        byName.put(gate.getName(), gate);
        gates.add(gate);
        if (gate instanceof InputGate inputGate) {
            inputs.add(inputGate);
        }
        topoOrder = null;
        return gate;
    }

    /** Wires the output of 'from' into an input of 'to'. */
    public void connect(Gate from, Gate to) {
        requireMember(from);
        requireMember(to);
        if (from == to) {
            throw new CircuitException("gate " + from.getName() + " cannot feed itself");
        }
        if (to.getInputs().size() >= to.maxInputs()) {
            throw new CircuitException("gate " + to.getName()
                    + " already has the maximum of " + to.maxInputs() + " input(s)");
        }
        to.addInput(from);
        topoOrder = null;
    }

    /** Marks a gate whose value counts as a result of the circuit. */
    public void markOutput(Gate gate) {
        requireMember(gate);
        if (outputs.contains(gate)) {
            throw new CircuitException("gate " + gate.getName() + " is already an output");
        }
        outputs.add(gate);
    }

    /** Checks that the circuit is complete. Throws CircuitException describing the first problem found. */
    public void validate() {
        if (gates.isEmpty()) {
            throw new CircuitException("circuit has no gates");
        }
        if (outputs.isEmpty()) {
            throw new CircuitException("circuit has no outputs");
        }
        for (Gate g : gates) {
            int n = g.getInputs().size();
            if (n < g.minInputs()) {
                throw new CircuitException("gate " + g.getName() + " has " + n
                        + " input(s) but needs at least " + g.minInputs());
            }
            if (n > g.maxInputs()) {
                throw new CircuitException("gate " + g.getName() + " has " + n
                        + " input(s) but allows at most " + g.maxInputs());
            }
        }
    }

    /**
     * Returns the gates in an order where every gate comes after all the gates that feed it
     * (Kahn's algorithm). The result is cached until the circuit changes.
     *
     * @throws CycleDetectedException if the wiring contains a loop
     */
    public List<Gate> getTopologicalOrder() {
        if (topoOrder != null) {
            return topoOrder;
        }

        // remaining.get(g) = how many of g's inputs have not been placed in the order yet
        Map<Gate, Integer> remaining = new HashMap<>();
        Deque<Gate> ready = new ArrayDeque<>();
        for (Gate g : gates) {
            int n = g.getInputs().size();
            remaining.put(g, n);
            if (n == 0) {
                ready.add(g);
            }
        }

        List<Gate> order = new ArrayList<>(gates.size());
        while (!ready.isEmpty()) {
            Gate g = ready.poll();
            order.add(g);
            for (Gate next : g.getFanout()) {
            	//left is basically integer
                int left = remaining.get(next) - 1;
                remaining.put(next, left);
                if (left == 0) {
                    ready.add(next);
                }
            }
        }

        if (order.size() != gates.size()) {
            // Gates still waiting on something never got ordered. That includes the gates
            // on the loop AND any gates downstream of it.
            StringBuilder names = new StringBuilder();
            int stuck = 0;
            for (Gate g : gates) {
                if (remaining.get(g) > 0) {
                    if (stuck < 5) {
                        if (stuck > 0) {
                            names.append(", ");
                        }
                        names.append(g.getName());
                    }
                    stuck++;
                }
            }
            throw new CycleDetectedException("cycle detected: " + stuck
                    + " gate(s) could not be ordered (e.g. " + names + ")");
        }

        topoOrder = Collections.unmodifiableList(order);
        return topoOrder;
    }

    public Gate getGate(String name) {
        Gate g = byName.get(name);
        if (g == null) {
            throw new CircuitException("no gate named " + name);
        }
        return g;
    }

    public int size() { return gates.size(); }
    public List<Gate> getGates() { return Collections.unmodifiableList(gates); }
    public List<InputGate> getInputs() { return Collections.unmodifiableList(inputs); }
    public List<Gate> getOutputs() { return Collections.unmodifiableList(outputs); }

    private void requireMember(Gate gate) {
        if (gate == null) {
            throw new CircuitException("gate cannot be null");
        }
        if (byName.get(gate.getName()) != gate) {
            throw new CircuitException("gate " + gate.getName() + " is not part of this circuit");
        }
    }
}