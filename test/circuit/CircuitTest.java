package circuit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import exceptions.CircuitException;
import gates.*;

class CircuitTest {

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

    @Test void validHalfAdderPasses() {
        Circuit c = halfAdder();
        assertDoesNotThrow(c::validate);
        assertEquals(4, c.size());
        assertEquals(2, c.getInputs().size());
        assertEquals(2, c.getOutputs().size());
    }

    @Test void lookupByName() {
        Circuit c = halfAdder();
        assertEquals("SUM", c.getGate("SUM").getName());
        assertThrows(CircuitException.class, () -> c.getGate("NOPE"));
    }

    @Test void duplicateNameRejected() {
        Circuit c = new Circuit();
        c.add(new InputGate("A"));
        assertThrows(CircuitException.class, () -> c.add(new AndGate("A")));
    }

    @Test void connectingForeignGateRejected() {
        Circuit c = new Circuit();
        Gate and = c.add(new AndGate("G"));
        InputGate outsider = new InputGate("X");   // never added to c
        assertThrows(CircuitException.class, () -> c.connect(outsider, and));
    }

    @Test void selfLoopRejected() {
        Circuit c = new Circuit();
        Gate and = c.add(new AndGate("G"));
        assertThrows(CircuitException.class, () -> c.connect(and, and));
    }

    @Test void inputGateCannotReceiveWire() {
        Circuit c = new Circuit();
        InputGate a = c.add(new InputGate("A"));
        InputGate b = c.add(new InputGate("B"));
        assertThrows(CircuitException.class, () -> c.connect(a, b));
    }

    @Test void notGateAllowsOnlyOneInput() {
        Circuit c = new Circuit();
        InputGate a = c.add(new InputGate("A"));
        InputGate b = c.add(new InputGate("B"));
        Gate not = c.add(new NotGate("N"));
        c.connect(a, not);
        assertThrows(CircuitException.class, () -> c.connect(b, not));
    }

    @Test void validateRejectsGateWithTooFewInputs() {
        Circuit c = new Circuit();
        InputGate a = c.add(new InputGate("A"));
        Gate and = c.add(new AndGate("G"));
        c.connect(a, and);                 // AND needs 2
        c.markOutput(and);
        assertThrows(CircuitException.class, c::validate);
    }

    @Test void validateRejectsNoOutputs() {
        Circuit c = new Circuit();
        c.add(new InputGate("A"));
        assertThrows(CircuitException.class, c::validate);
    }

    @Test void validateRejectsEmptyCircuit() {
        assertThrows(CircuitException.class, () -> new Circuit().validate());
    }

    @Test void duplicateOutputRejected() {
        Circuit c = halfAdder();
        Gate sum = c.getGate("SUM");
        assertThrows(CircuitException.class, () -> c.markOutput(sum));
    }
}