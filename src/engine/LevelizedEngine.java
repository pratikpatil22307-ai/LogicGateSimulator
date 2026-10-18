package engine;

import java.util.List;
import java.util.Objects;

import circuit.Circuit;
import exceptions.CircuitException;
import gates.Gate;
import gates.InputGate;

/** Evaluates every gate exactly once, in topological order. */
public class LevelizedEngine implements SimulationEngine {

    private int lastEvaluationCount;

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

        // 2. one pass in dependency order (throws CycleDetectedException on a loop)
        List<Gate> order = circuit.getTopologicalOrder();
        for (Gate g : order) {
            g.update();
        }
        lastEvaluationCount = order.size();

        // 3. read the outputs
        List<Gate> outs = circuit.getOutputs();
        boolean[] result = new boolean[outs.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = outs.get(i).getOutput();
        }
        return result;
    }

    @Override
    public String getName() { return "Levelized"; }

    /** How many gate evaluations the last simulate() call performed. */
    public int getLastEvaluationCount() { return lastEvaluationCount; }
}