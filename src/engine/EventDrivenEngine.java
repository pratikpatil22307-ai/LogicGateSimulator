package engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import circuit.Circuit;
import exceptions.CircuitException;
import gates.Gate;
import gates.InputGate;

/**
 * Event-driven engine: a gate is re-evaluated only when something feeding it has changed.
 * Part 1: every run starts cold (all gates waiting), so each gate is evaluated once.
 */
public class EventDrivenEngine implements SimulationEngine {

    // Tables built from the circuit's topological order. Rebuilt when the circuit changes.
    private List<Gate> preparedOrder;   // the order the tables were built from
    private Gate[] gateAt;              // gateAt[p] = the gate at position p
    private int[][] fanoutAt;           // fanoutAt[p] = positions of the gates fed by gateAt[p]
    private EventQueue queue;

    private int lastEvaluationCount;

    @Override
    public boolean[] simulate(Circuit circuit, boolean[] inputValues) {
        Objects.requireNonNull(circuit, "circuit cannot be null");
        Objects.requireNonNull(inputValues, "inputValues cannot be null");
        circuit.validate();

        List<InputGate> inputs = circuit.getInputs();
        if (inputValues.length != inputs.size()) {
            throw new CircuitException("circuit has " + inputs.size()
                    + " input(s) but " + inputValues.length + " value(s) were given");
        }

        // throws CycleDetectedException on a loop, before anything has been changed
        List<Gate> order = circuit.getTopologicalOrder();
        if (order != preparedOrder) {   // same list object means the circuit has not changed
            prepare(order);
        }

        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).setValue(inputValues[i]);
        }

        // cold start: every gate is waiting
        for (int p = 0; p < gateAt.length; p++) {
            queue.schedule(p);
        }
        lastEvaluationCount = runQueue();

        List<Gate> outs = circuit.getOutputs();
        boolean[] result = new boolean[outs.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = outs.get(i).getOutput();
        }
        return result;
    }

    /** Builds the lookup tables: where each gate sits in the order, and where its fanout sits. */
    private void prepare(List<Gate> order) {
        int n = order.size();
        Map<Gate, Integer> position = new HashMap<>();
        gateAt = new Gate[n];
        for (int p = 0; p < n; p++) {
            gateAt[p] = order.get(p);
            position.put(gateAt[p], p);
        }

        fanoutAt = new int[n][];
        for (int p = 0; p < n; p++) {
            List<Gate> fanout = gateAt[p].getFanout();
            fanoutAt[p] = new int[fanout.size()];
            for (int k = 0; k < fanout.size(); k++) {
                fanoutAt[p][k] = position.get(fanout.get(k));
            }
        }

        queue = new EventQueue(n);
        preparedOrder = order;
    }

    /** The heart of the engine: take a waiting gate, update it, and wake its fanout if it changed. */
    private int runQueue() {
        int evaluations = 0;
        while (!queue.isEmpty()) {
            int p = queue.poll();
            evaluations++;
            if (gateAt[p].update()) {
                for (int q : fanoutAt[p]) {
                    queue.schedule(q);
                }
            }
        }
        return evaluations;
    }

    @Override
    public String getName() { return "EventDriven"; }

    /** How many gate evaluations the last simulate() call performed. */
    public int getLastEvaluationCount() { return lastEvaluationCount; }
}