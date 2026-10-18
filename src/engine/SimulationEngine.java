package engine;

import circuit.Circuit;

public interface SimulationEngine {

    /**
     * Runs the circuit for the given input values and returns the output values.
     * inputValues[i] goes to the i-th input gate (in the order the inputs were added);
     * result[i] is the value of the i-th output gate (in the order the outputs were marked).
     */
    boolean[] simulate(Circuit circuit, boolean[] inputValues);

    String getName();
}