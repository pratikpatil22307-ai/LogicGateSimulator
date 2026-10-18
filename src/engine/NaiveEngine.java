package engine;

import java.util.List;
import java.util.Objects;

import circuit.Circuit;
import exceptions.CircuitException;
import gates.Gate;
import gates.InputGate;

public class NaiveEngine implements SimulationEngine {

    private int lastPassCount;

    @Override
    public boolean[] simulate(Circuit circuit, boolean[] inputValues) {
        Objects.requireNonNull(circuit, "circuit cannot be null");
        Objects.requireNonNull(inputValues, "inputValues cannot be null");
        circuit.validate();

        // 1. apply the input values
        List<InputGate> inputs = circuit.getInputs();
        if (inputValues.length != inputs.size()) {
            throw new CircuitException("circuit has " + inputs.size()
                    + " input(s) but " + inputValues.length + " value(s) were given");
        }
        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).setValue(inputValues[i]);
        }

        // 2. sweep over all gates until a whole sweep changes nothing
        List<Gate> gates = circuit.getGates();
        int maxPasses = gates.size() + 1;
        boolean settled = false;
        lastPassCount = 0;
        while (!settled && lastPassCount < maxPasses) {
            lastPassCount++;
            boolean changed = false;
            for (Gate g : gates) {
                if (g.update()) {
                    changed = true;
                }
            }
            settled = !changed;
        }
        if (!settled) {
            throw new CircuitException("circuit did not settle after " + maxPasses
                    + " passes (is there a loop in the wiring?)");
        }

        // 3. read the outputs
        List<Gate> outs = circuit.getOutputs();
        boolean[] result = new boolean[outs.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = outs.get(i).getOutput();
        }
        return result;
    }

    @Override
    public String getName() { return "Naive"; }

    /** How many sweeps the last simulate() call needed. Useful for understanding and benchmarks. */
    public int getLastPassCount() { return lastPassCount; }
}