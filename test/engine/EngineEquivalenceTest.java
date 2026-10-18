package engine;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import circuit.Circuit;
import gates.*;
import generator.AdderGenerator;

class EngineEquivalenceTest {

    /** Every engine you build goes in this list. Day 14 adds the event-driven engine. */
    private static List<SimulationEngine> engines() {
        return List.of(new NaiveEngine(), new LevelizedEngine(), new EventDrivenEngine());
    }

    private static List<boolean[]> randomVectors(int width, int count, Random rnd) {
        List<boolean[]> vectors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            boolean[] v = new boolean[width];
            for (int j = 0; j < width; j++) {
                v[j] = rnd.nextBoolean();
            }
            vectors.add(v);
        }
        return vectors;
    }

    /** Runs the same sequence of inputs through a naive reference and every engine; results must match. */
    private static void assertEnginesAgree(Circuit c, List<boolean[]> vectors, String label) {
        NaiveEngine reference = new NaiveEngine();
        List<SimulationEngine> candidates = engines();
        for (int i = 0; i < vectors.size(); i++) {
            boolean[] expected = reference.simulate(c, vectors.get(i));
            for (SimulationEngine e : candidates) {
                assertArrayEquals(expected, e.simulate(c, vectors.get(i)),
                        e.getName() + " differs from naive on " + label + ", vector " + i);
            }
        }
    }

    /** A random circuit with no loops: every gate takes its inputs from nodes created before it. */
    private static Circuit randomCircuit(int inputCount, int gateCount, Random rnd) {
        int total = inputCount + gateCount;
        Gate[] nodes = new Gate[total];
        for (int i = 0; i < inputCount; i++) {
            nodes[i] = new InputGate("I" + i);
        }
        GateType[] kinds = { GateType.AND, GateType.OR, GateType.NAND, GateType.NOR,
                             GateType.XOR, GateType.XNOR, GateType.NOT };
        for (int i = inputCount; i < total; i++) {
            nodes[i] = GateFactory.create(kinds[rnd.nextInt(kinds.length)], "G" + i);
        }

        // add the gates in a shuffled order, so insertion order says nothing about dependencies
        List<Gate> addOrder = new ArrayList<>(Arrays.asList(nodes));
        Collections.shuffle(addOrder, rnd);
        Circuit c = new Circuit();
        for (Gate g : addOrder) {
            c.add(g);
        }

        for (int i = inputCount; i < total; i++) {
            int fanIn = (nodes[i] instanceof NotGate) ? 1 : 2 + rnd.nextInt(2);   // 1, or 2 to 3
            for (int k = 0; k < fanIn; k++) {
                c.connect(nodes[rnd.nextInt(i)], nodes[i]);                         // only from earlier nodes
            }
        }
        for (int i = total - Math.min(4, gateCount); i < total; i++) {
            c.markOutput(nodes[i]);
        }
        return c;
    }

    @Test void fourBitAdderExhaustive() {
        Circuit c = AdderGenerator.rippleCarry(4);
        for (SimulationEngine e : engines()) {
            for (int a = 0; a < 16; a++) {
                for (int b = 0; b < 16; b++) {
                    for (int cin = 0; cin < 2; cin++) {
                        boolean[] out = e.simulate(c,
                                AdderGenerator.packInputs(BigInteger.valueOf(a), BigInteger.valueOf(b), cin == 1, 4));
                        assertEquals(BigInteger.valueOf(a + b + cin), AdderGenerator.unpackSum(out, 4),
                                e.getName() + ": " + a + " + " + b + " + " + cin);
                    }
                }
            }
        }
    }

    @Test void widerAddersMatchBigIntegerOnEveryEngine() {
        Random rnd = new Random(3);
        for (int width : new int[] { 1, 8, 64, 256 }) {
            Circuit c = AdderGenerator.rippleCarry(width);
            for (SimulationEngine e : engines()) {
                for (int i = 0; i < 20; i++) {
                    BigInteger a = new BigInteger(width, rnd);
                    BigInteger b = new BigInteger(width, rnd);
                    boolean cin = rnd.nextBoolean();
                    BigInteger expected = a.add(b).add(cin ? BigInteger.ONE : BigInteger.ZERO);
                    boolean[] out = e.simulate(c, AdderGenerator.packInputs(a, b, cin, width));
                    assertEquals(expected, AdderGenerator.unpackSum(out, width),
                            e.getName() + ", width " + width);
                }
            }
        }
    }

    @Test void randomCircuitsAgree() {
        for (int seed = 0; seed < 10; seed++) {
            Random rnd = new Random(seed);
            Circuit c = randomCircuit(6, 300, rnd);
            assertEnginesAgree(c, randomVectors(6, 30, rnd), "random circuit, seed " + seed);
        }
    }

    @Test void largeRandomCircuitAgrees() {
        Random rnd = new Random(99);
        Circuit c = randomCircuit(20, 3000, rnd);
        assertEnginesAgree(c, randomVectors(20, 10, rnd), "large random circuit");
    }
}