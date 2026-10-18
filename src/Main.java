import java.math.BigInteger;
import java.util.Arrays;

import circuit.Circuit;
import engine.LevelizedEngine;
import engine.NaiveEngine;
import exceptions.CycleDetectedException;
import gates.*;
import generator.AdderGenerator;
import generator.ChainGenerator;
import util.TruthTableGenerator;
import engine.EventDrivenEngine;

public class Main {
    public static void main(String[] args) {
        NaiveEngine engine = new NaiveEngine();
        TruthTableGenerator table = new TruthTableGenerator(engine);

        System.out.println("Half adder");
        table.print(AdderGenerator.halfAdder());
        System.out.println();
        System.out.println("Full adder");
        table.print(AdderGenerator.fullAdder());
        System.out.println();

        int width = 16;
        Circuit adder = AdderGenerator.rippleCarry(width);
        System.out.println(width + "-bit ripple-carry adder: " + adder.size() + " gates");

        BigInteger a = BigInteger.valueOf(12345);
        BigInteger b = BigInteger.valueOf(54321);
        boolean[] out = engine.simulate(adder, AdderGenerator.packInputs(a, b, false, width));
        BigInteger sum = AdderGenerator.unpackSum(out, width);

        System.out.println(a + " + " + b + " = " + sum + " (circuit), " + a.add(b) + " (Java)");
        System.out.println("Sweeps needed: " + engine.getLastPassCount());
        
        EventDrivenEngine event = new EventDrivenEngine();
        boolean[] out2 = event.simulate(adder, AdderGenerator.packInputs(a, b, false, width));
        System.out.println("Event-driven result: " + AdderGenerator.unpackSum(out2, width)
                + ", evaluations: " + event.getLastEvaluationCount());

        System.out.println();
        Circuit half = AdderGenerator.halfAdder();
        System.out.print("Evaluation order:");
        for (Gate g : half.getTopologicalOrder()) {
            System.out.print(" " + g.getName());
        }
        System.out.println();

        Circuit ring = new Circuit();
        Gate g1 = ring.add(new NotGate("G1"));
        Gate g2 = ring.add(new NotGate("G2"));
        Gate g3 = ring.add(new NotGate("G3"));
        ring.connect(g3, g1);
        ring.connect(g1, g2);
        ring.connect(g2, g3);
        try {
            ring.getTopologicalOrder();
        } catch (CycleDetectedException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        chainDemo();
    }

    static void chainDemo() {
        int n = 500;
        Circuit chain = ChainGenerator.notChain(n, true);
        NaiveEngine naive = new NaiveEngine();
        LevelizedEngine levelized = new LevelizedEngine();
        boolean[] input = { true };

        boolean[] r1 = naive.simulate(chain, input);
        boolean[] r2 = levelized.simulate(chain, input);

        System.out.println();
        System.out.println("Chain of " + n + " NOT gates, added in the worst order");
        System.out.println("  results equal: " + Arrays.equals(r1, r2));
        System.out.println("  naive:     " + naive.getLastPassCount() + " sweeps = "
                + (long) naive.getLastPassCount() * chain.size() + " gate evaluations");
        System.out.println("  levelized: 1 pass  = " + levelized.getLastEvaluationCount() + " gate evaluations");
    }
    
    
}