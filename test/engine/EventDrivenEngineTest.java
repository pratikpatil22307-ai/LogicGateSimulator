package engine;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.Random;

import org.junit.jupiter.api.Test;

import circuit.Circuit;
import exceptions.CircuitException;
import exceptions.CycleDetectedException;
import gates.*;
import generator.AdderGenerator;
import generator.ChainGenerator;

class EventDrivenEngineTest {

    private final EventDrivenEngine engine = new EventDrivenEngine();

    @Test void halfAdderMatchesArithmetic() {
        Circuit c = AdderGenerator.halfAdder();
        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 2; b++) {
                boolean[] out = engine.simulate(c, new boolean[] { a == 1, b == 1 });
                assertEquals((a + b) % 2 == 1, out[0], "SUM " + a + "," + b);
                assertEquals(a + b == 2, out[1], "CARRY " + a + "," + b);
            }
        }
    }

    @Test void firstRunEvaluatesEveryGateOnce() {
        Circuit c = AdderGenerator.rippleCarry(16);
        engine.simulate(c, new boolean[33]);          // 16 + 16 + 1 inputs, all false
        assertEquals(113, c.size());
        assertEquals(c.size(), engine.getLastEvaluationCount());
    }

    @Test void gateAddOrderDoesNotMatter() {
        Circuit c = new Circuit();
        Gate sum = c.add(new XorGate("SUM"));          // outputs added before inputs
        Gate carry = c.add(new AndGate("CARRY"));
        InputGate a = c.add(new InputGate("A"));
        InputGate b = c.add(new InputGate("B"));
        c.connect(a, sum);
        c.connect(b, sum);
        c.connect(a, carry);
        c.connect(b, carry);
        c.markOutput(sum);
        c.markOutput(carry);

        assertArrayEquals(new boolean[] { false, true }, engine.simulate(c, new boolean[] { true, true }));
        assertEquals(4, engine.getLastEvaluationCount());
    }

    @Test void reversedChainIsHandled() {
        int n = 50;
        Circuit c = ChainGenerator.notChain(n, true);
        boolean[] out = engine.simulate(c, new boolean[] { true });
        assertEquals(n % 2 == 0, out[0]);
        assertEquals(n + 1, engine.getLastEvaluationCount());

        out = engine.simulate(c, new boolean[] { false });
        assertEquals(n % 2 == 1, out[0]);
    }

    @Test void largeAddersMatchBigInteger() {
        Random rnd = new Random(11);
        for (int width : new int[] { 8, 64, 256 }) {
            Circuit c = AdderGenerator.rippleCarry(width);
            for (int i = 0; i < 10; i++) {
                BigInteger a = new BigInteger(width, rnd);
                BigInteger b = new BigInteger(width, rnd);
                boolean cin = rnd.nextBoolean();
                BigInteger expected = a.add(b).add(cin ? BigInteger.ONE : BigInteger.ZERO);
                boolean[] out = engine.simulate(c, AdderGenerator.packInputs(a, b, cin, width));
                assertEquals(expected, AdderGenerator.unpackSum(out, width), "width " + width);
            }
        }
    }

    @Test void resultsDoNotDependOnPreviousRuns() {
        Circuit c = AdderGenerator.halfAdder();
        int[] order = { 3, 0, 2, 1, 3, 3, 0 };
        for (int i : order) {
            boolean a = (i & 2) != 0;
            boolean b = (i & 1) != 0;
            assertArrayEquals(new boolean[] { a ^ b, a & b }, engine.simulate(c, new boolean[] { a, b }),
                    "input " + i);
        }
    }

    @Test void editedCircuitIsSimulatedCorrectly() {
        Circuit c = AdderGenerator.halfAdder();
        assertArrayEquals(new boolean[] { true, false }, engine.simulate(c, new boolean[] { true, false }));

        Gate extra = c.add(new NotGate("EXTRA"));      // makes the circuit hand out a new order
        c.connect(c.getGate("SUM"), extra);
        c.markOutput(extra);

        // SUM = 1, CARRY = 0, EXTRA = not SUM = 0
        assertArrayEquals(new boolean[] { true, false, false },
                engine.simulate(c, new boolean[] { true, false }));
        assertEquals(5, engine.getLastEvaluationCount());
    }

    @Test void sameEngineCanServeTwoCircuits() {
        Circuit half = AdderGenerator.halfAdder();
        Circuit full = AdderGenerator.fullAdder();
        for (int round = 0; round < 3; round++) {
            assertArrayEquals(new boolean[] { false, true },
                    engine.simulate(half, new boolean[] { true, true }), "half adder, round " + round);
            assertArrayEquals(new boolean[] { true, true },
                    engine.simulate(full, new boolean[] { true, true, true }), "full adder, round " + round);
        }
    }

    @Test void loopIsReportedAsCycle() {
        Circuit c = new Circuit();
        Gate g1 = c.add(new NotGate("G1"));
        Gate g2 = c.add(new NotGate("G2"));
        Gate g3 = c.add(new NotGate("G3"));
        c.connect(g3, g1);
        c.connect(g1, g2);
        c.connect(g2, g3);
        c.markOutput(g1);
        assertThrows(CycleDetectedException.class, () -> engine.simulate(c, new boolean[0]));
    }

    @Test void wrongNumberOfInputValuesRejected() {
        Circuit c = AdderGenerator.halfAdder();
        assertThrows(CircuitException.class, () -> engine.simulate(c, new boolean[] { true }));
    }

    @Test void invalidCircuitRejected() {
        assertThrows(CircuitException.class, () -> engine.simulate(new Circuit(), new boolean[0]));
    }

    @Test void engineHasName() {
        assertEquals("EventDriven", engine.getName());
    }
}