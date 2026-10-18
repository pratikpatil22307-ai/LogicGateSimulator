package util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import circuit.Circuit;
import engine.NaiveEngine;
import exceptions.CircuitException;
import gates.*;

class TruthTableGeneratorTest {

    private final TruthTableGenerator gen = new TruthTableGenerator(new NaiveEngine());

    private Circuit halfAdder() {
        Circuit c = new Circuit();
        InputGate a = c.add(new InputGate("A"));
        InputGate b = c.add(new InputGate("B"));
        Gate sum = c.add(new XorGate("SUM"));
        Gate carry = c.add(new AndGate("CARRY"));
        c.connect(a, sum);
        c.connect(b, sum);
        c.connect(a, carry);
        c.connect(b, carry);
        c.markOutput(sum);
        c.markOutput(carry);
        return c;
    }

    private Circuit fullAdder() {
        Circuit c = new Circuit();
        InputGate a = c.add(new InputGate("A"));
        InputGate b = c.add(new InputGate("B"));
        InputGate cin = c.add(new InputGate("CIN"));
        Gate x1 = c.add(new XorGate("X1"));
        Gate sum = c.add(new XorGate("SUM"));
        Gate a1 = c.add(new AndGate("A1"));
        Gate a2 = c.add(new AndGate("A2"));
        Gate cout = c.add(new OrGate("COUT"));
        c.connect(a, x1);
        c.connect(b, x1);
        c.connect(x1, sum);
        c.connect(cin, sum);
        c.connect(a, a1);
        c.connect(b, a1);
        c.connect(x1, a2);
        c.connect(cin, a2);
        c.connect(a1, cout);
        c.connect(a2, cout);
        c.markOutput(sum);
        c.markOutput(cout);
        return c;
    }

    @Test void halfAdderTable() {
        boolean[][] expected = {
            { false, false, false, false },   // 0 0 -> sum 0, carry 0
            { false, true,  true,  false },   // 0 1 -> sum 1, carry 0
            { true,  false, true,  false },   // 1 0 -> sum 1, carry 0
            { true,  true,  false, true  },   // 1 1 -> sum 0, carry 1
        };
        assertArrayEquals(expected, gen.generate(halfAdder()));
    }

    @Test void fullAdderMatchesArithmetic() {
        boolean[][] table = gen.generate(fullAdder());
        for (int r = 0; r < 8; r++) {
            int a = (r >> 2) & 1;
            int b = (r >> 1) & 1;
            int cin = r & 1;
            int total = a + b + cin;
            boolean[] row = table[r];
            assertEquals(a == 1, row[0], "A, row " + r);
            assertEquals(b == 1, row[1], "B, row " + r);
            assertEquals(cin == 1, row[2], "CIN, row " + r);
            assertEquals(total % 2 == 1, row[3], "SUM, row " + r);
            assertEquals(total >= 2, row[4], "COUT, row " + r);
        }
    }

    @Test void rowAndColumnCounts() {
        boolean[][] half = gen.generate(halfAdder());
        assertEquals(4, half.length);
        assertEquals(4, half[0].length);          // 2 inputs + 2 outputs

        boolean[][] full = gen.generate(fullAdder());
        assertEquals(8, full.length);
        assertEquals(5, full[0].length);          // 3 inputs + 2 outputs
    }

    @Test void formatHalfAdder() {
        String[] lines = gen.format(halfAdder()).split("\n");
        assertEquals(5, lines.length);
        assertEquals("A B | SUM CARRY", lines[0]);
        assertEquals("0 0 | 0   0", lines[1]);
        assertEquals("1 1 | 0   1", lines[4]);
    }

    @Test void formatFullAdder() {
        String[] lines = gen.format(fullAdder()).split("\n");
        assertEquals(9, lines.length);
        assertEquals("A B CIN | SUM COUT", lines[0]);
        assertEquals("0 0 0   | 0   0", lines[1]);
        assertEquals("1 1 1   | 1   1", lines[8]);
    }

    @Test void tooManyInputsRejected() {
        Circuit c = new Circuit();
        Gate and = c.add(new AndGate("G"));
        for (int i = 0; i < TruthTableGenerator.MAX_INPUTS + 1; i++) {
            InputGate in = c.add(new InputGate("I" + i));
            c.connect(in, and);
        }
        c.markOutput(and);
        assertThrows(CircuitException.class, () -> gen.generate(c));
    }

    @Test void nullEngineRejected() {
        assertThrows(NullPointerException.class, () -> new TruthTableGenerator(null));
    }
}