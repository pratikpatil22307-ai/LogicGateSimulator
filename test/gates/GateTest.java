package gates;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

class GateTest {

    /** Wires fresh input gates to the given gate, evaluates, returns its output. */
    private static boolean run(Gate gate, boolean... values) {
        for (int i = 0; i < values.length; i++) {
            InputGate in = new InputGate("in" + i);
            in.setValue(values[i]);
            in.update();
            gate.addInput(in);
        }
        gate.update();
        return gate.getOutput();
    }

    // expected values are listed for inputs 00, 01, 10, 11
    private static void check2(Supplier<Gate> maker, boolean... expected) {
        boolean[][] in = { {false, false}, {false, true}, {true, false}, {true, true} };
        for (int i = 0; i < 4; i++) {
            assertEquals(expected[i], run(maker.get(), in[i]),
                    "inputs " + in[i][0] + "," + in[i][1]);
        }
    }

    @Test void andGate()  { check2(() -> new AndGate("g"),  false, false, false, true); }
    @Test void orGate()   { check2(() -> new OrGate("g"),   false, true,  true,  true); }
    @Test void nandGate() { check2(() -> new NandGate("g"), true,  true,  true,  false); }
    @Test void norGate()  { check2(() -> new NorGate("g"),  true,  false, false, false); }
    @Test void xorGate()  { check2(() -> new XorGate("g"),  false, true,  true,  false); }
    @Test void xnorGate() { check2(() -> new XnorGate("g"), true,  false, false, true); }

    @Test void notGate() {
        assertTrue(run(new NotGate("g"), false));
        assertFalse(run(new NotGate("g"), true));
    }

    @Test void xorThreeInputsIsParity() {
        assertTrue(run(new XorGate("g"), true, true, true));    // three 1s: odd
        assertFalse(run(new XorGate("g"), true, true, false));  // two 1s: even
    }

    @Test void factoryCreatesCorrectTypes() {
        assertInstanceOf(AndGate.class, GateFactory.create(GateType.AND, "g"));
        assertInstanceOf(XnorGate.class, GateFactory.create(GateType.XNOR, "g"));
        assertInstanceOf(InputGate.class, GateFactory.create(GateType.INPUT, "g"));
    }

    @Test void gateCannotFeedItself() {
        Gate g = new AndGate("g");
        assertThrows(IllegalArgumentException.class, () -> g.addInput(g));
    }

    @Test void inputGateRejectsInputs() {
        InputGate in = new InputGate("in");
        assertThrows(UnsupportedOperationException.class, () -> in.addInput(new InputGate("x")));
    }

    @Test void updateReportsChange() {
        InputGate in = new InputGate("in");
        in.setValue(true);
        assertTrue(in.update());    // false -> true: changed
        assertFalse(in.update());   // true -> true: no change
    }
}