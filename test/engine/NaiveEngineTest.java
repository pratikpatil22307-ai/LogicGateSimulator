package engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import circuit.Circuit;
import exceptions.CircuitException;
import gates.*;

class NaiveEngineTest {

    /** Builds a half adder. If inputsFirst is false, the gates are added in the "wrong" order. */
    private Circuit halfAdder(boolean inputsFirst) {
        Circuit c = new Circuit();
        InputGate a;
        InputGate b;
        Gate sum;
        Gate carry;
        if (inputsFirst) {
            a = c.add(new InputGate("A"));
            b = c.add(new InputGate("B"));
            sum = c.add(new XorGate("SUM"));
            carry = c.add(new AndGate("CARRY"));
        } else {
            sum = c.add(new XorGate("SUM"));
            carry = c.add(new AndGate("CARRY"));
            a = c.add(new InputGate("A"));
            b = c.add(new InputGate("B"));
        }
        c.connect(a, sum);
        c.connect(b, sum);
        c.connect(a, carry);
        c.connect(b, carry);
        c.markOutput(sum);
        c.markOutput(carry);
        return c;
    }

    @Test void halfAdderTruthTable() {
        Circuit c = halfAdder(true);
        NaiveEngine e = new NaiveEngine();
        assertArrayEquals(new boolean[] { false, false }, e.simulate(c, new boolean[] { false, false }));
        assertArrayEquals(new boolean[] { true,  false }, e.simulate(c, new boolean[] { false, true  }));
        assertArrayEquals(new boolean[] { true,  false }, e.simulate(c, new boolean[] { true,  false }));
        assertArrayEquals(new boolean[] { false, true  }, e.simulate(c, new boolean[] { true,  true  }));
    }

    @Test void gateAddOrderDoesNotChangeResults() {
        Circuit forward = halfAdder(true);
        Circuit reverse = halfAdder(false);
        NaiveEngine e = new NaiveEngine();
        for (int i = 0; i < 4; i++) {
            boolean[] in = { (i & 2) != 0, (i & 1) != 0 };
            assertArrayEquals(e.simulate(forward, in), e.simulate(reverse, in), "input " + i);
        }
    }

    @Test void resultsAreRepeatableInAnyOrder() {
        Circuit c = halfAdder(true);
        NaiveEngine e = new NaiveEngine();
        int[] order = { 3, 0, 2, 1, 3, 3, 0 };
        for (int i : order) {
            boolean a = (i & 2) != 0;
            boolean b = (i & 1) != 0;
            assertArrayEquals(new boolean[] { a ^ b, a & b }, e.simulate(c, new boolean[] { a, b }),
                    "input " + i);
        }
    }

    @Test void notGate() {
        Circuit c = new Circuit();
        InputGate a = c.add(new InputGate("A"));
        Gate n = c.add(new NotGate("N"));
        c.connect(a, n);
        c.markOutput(n);
        NaiveEngine e = new NaiveEngine();
        assertArrayEquals(new boolean[] { true },  e.simulate(c, new boolean[] { false }));
        assertArrayEquals(new boolean[] { false }, e.simulate(c, new boolean[] { true }));
    }

    @Test void wrongNumberOfInputValuesRejected() {
        Circuit c = halfAdder(true);
        assertThrows(CircuitException.class, () -> new NaiveEngine().simulate(c, new boolean[] { true }));
    }

    @Test void invalidCircuitRejected() {
        assertThrows(CircuitException.class, () -> new NaiveEngine().simulate(new Circuit(), new boolean[0]));
    }

    @Test void ringOfThreeNotGatesIsDetected() {
        Circuit c = new Circuit();
        Gate g1 = c.add(new NotGate("G1"));
        Gate g2 = c.add(new NotGate("G2"));
        Gate g3 = c.add(new NotGate("G3"));
        c.connect(g3, g1);
        c.connect(g1, g2);
        c.connect(g2, g3);
        c.markOutput(g1);
        // each NOT passes validate(), but the ring never settles
        assertThrows(CircuitException.class, () -> new NaiveEngine().simulate(c, new boolean[0]));
    }

    @Test void gateOrderAffectsNumberOfPasses() {
        NaiveEngine forward = new NaiveEngine();
        forward.simulate(halfAdder(true), new boolean[] { true, true });
        assertEquals(2, forward.getLastPassCount());

        NaiveEngine reverse = new NaiveEngine();
        reverse.simulate(halfAdder(false), new boolean[] { true, true });
        assertEquals(3, reverse.getLastPassCount());
    }

    @Test void engineHasName() {
        assertEquals("Naive", new NaiveEngine().getName());
    }
}