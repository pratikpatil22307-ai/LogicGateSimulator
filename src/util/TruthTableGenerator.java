package util;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import circuit.Circuit;
import engine.SimulationEngine;
import exceptions.CircuitException;
import gates.Gate;
import gates.InputGate;

public class TruthTableGenerator {

    /** 2^20 is about a million rows. More than that is not a table anyone can read. */
    public static final int MAX_INPUTS = 20;

    private final SimulationEngine engine;

    public TruthTableGenerator(SimulationEngine engine) {
        this.engine = Objects.requireNonNull(engine, "engine cannot be null");
    }

    /**
     * Returns one row per input combination. Each row holds the input values
     * followed by the output values.
     */
    public boolean[][] generate(Circuit circuit) {
        Objects.requireNonNull(circuit, "circuit cannot be null");
        circuit.validate();

        int n = circuit.getInputs().size();
        int m = circuit.getOutputs().size();
        if (n > MAX_INPUTS) {
            throw new CircuitException("truth table needs 2^" + n
                    + " rows; the limit is " + MAX_INPUTS + " inputs");
        }

        int rows = 1 << n;                       // 2^n
        boolean[][] table = new boolean[rows][n + m];
        for (int r = 0; r < rows; r++) {
            boolean[] in = new boolean[n];
            for (int i = 0; i < n; i++) {
                in[i] = ((r >> (n - 1 - i)) & 1) != 0;
            }
            boolean[] out = engine.simulate(circuit, in);
            System.arraycopy(in, 0, table[r], 0, n);
            System.arraycopy(out, 0, table[r], n, m);
        }
        return table;
    }

    /** Builds the table as text, with a header line of input and output names. */
    public String format(Circuit circuit) {
        boolean[][] table = generate(circuit);
        List<InputGate> ins = circuit.getInputs();
        List<Gate> outs = circuit.getOutputs();
        int n = ins.size();

        StringBuilder sb = new StringBuilder();
        sb.append(names(ins)).append(" | ").append(names(outs)).append('\n');

        for (boolean[] row : table) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if (i > 0) line.append(' ');
                line.append(pad(bit(row[i]), ins.get(i).getName().length()));
            }
            line.append(" | ");
            for (int j = 0; j < outs.size(); j++) {
                if (j > 0) line.append(' ');
                line.append(pad(bit(row[n + j]), outs.get(j).getName().length()));
            }
            sb.append(line.toString().stripTrailing()).append('\n');
        }
        return sb.toString();
    }

    public void print(Circuit circuit) {
        System.out.print(format(circuit));
    }

    private static String names(List<? extends Gate> gates) {
        return gates.stream().map(Gate::getName).collect(Collectors.joining(" "));
    }

    private static String bit(boolean b) { return b ? "1" : "0"; }

    private static String pad(String s, int width) {
        return String.format("%-" + Math.max(1, width) + "s", s);
    }
}