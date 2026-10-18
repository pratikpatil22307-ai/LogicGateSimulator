package circuit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import exceptions.CircuitException;
import exceptions.CycleDetectedException;
import gates.*;
import generator.AdderGenerator;

class TopologicalOrderTest {

    /** Checks the order holds every gate exactly once, with every gate after all of its inputs. */
    private static void assertValidOrder(Circuit c) {
        List<Gate> order = c.getTopologicalOrder();
        assertEquals(c.size(), order.size(), "order must contain every gate");

        Map<Gate, Integer> position = new HashMap<>();
        for (int i = 0; i < order.size(); i++) {
            assertNull(position.put(order.get(i), i), "gate listed twice: " + order.get(i).getName());
        }
        for (Gate g : order) {
            for (Gate in : g.getInputs()) {
                assertTrue(position.get(in) < position.get(g),
                        in.getName() + " must come before " + g.getName());
            }
        }
    }

    @Test void halfAdderOrderIsValid() {
        assertValidOrder(AdderGenerator.halfAdder());
    }

    @Test void gatesAddedInTheWrongOrderStillGetAValidOrder() {
        Circuit c = new Circuit();
        Gate sum = c.add(new XorGate("SUM"));        // outputs added before inputs
        Gate carry = c.add(new AndGate("CARRY"));
        InputGate a = c.add(new InputGate("A"));
        InputGate b = c.add(new InputGate("B"));
        c.connect(a, sum);
        c.connect(b, sum);
        c.connect(a, carry);
        c.connect(b, carry);
        c.markOutput(sum);
        c.markOutput(carry);
        assertValidOrder(c);
    }

    @Test void rippleCarry64HasValidOrder() {
        assertValidOrder(AdderGenerator.rippleCarry(64));
    }

    @Test void rippleCarry1024HasValidOrder() {
        assertValidOrder(AdderGenerator.rippleCarry(1024));
    }

    @Test void ringOfThreeNotGatesIsACycle() {
        Circuit c = new Circuit();
        Gate g1 = c.add(new NotGate("G1"));
        Gate g2 = c.add(new NotGate("G2"));
        Gate g3 = c.add(new NotGate("G3"));
        c.connect(g3, g1);
        c.connect(g1, g2);
        c.connect(g2, g3);
        assertThrows(CycleDetectedException.class, c::getTopologicalOrder);
    }

    @Test void gatesDownstreamOfALoopAreCountedAsStuck() {
        Circuit c = new Circuit();
        Gate g1 = c.add(new NotGate("G1"));
        Gate g2 = c.add(new NotGate("G2"));
        Gate g3 = c.add(new NotGate("G3"));    // not on the loop, but fed by it
        c.connect(g2, g1);
        c.connect(g1, g2);
        c.connect(g1, g3);
        CycleDetectedException e = assertThrows(CycleDetectedException.class, c::getTopologicalOrder);
        assertTrue(e.getMessage().contains("3 gate(s)"), e.getMessage());
    }

    @Test void cycleExceptionIsACircuitException() {
        Circuit c = new Circuit();
        Gate g1 = c.add(new NotGate("G1"));
        Gate g2 = c.add(new NotGate("G2"));
        c.connect(g2, g1);
        c.connect(g1, g2);
        assertThrows(CircuitException.class, c::getTopologicalOrder);
    }

    @Test void orderIsCachedUntilTheCircuitChanges() {
        Circuit c = AdderGenerator.halfAdder();
        List<Gate> first = c.getTopologicalOrder();
        assertSame(first, c.getTopologicalOrder(), "second call should reuse the cached order");

        Gate extra = c.add(new NotGate("EXTRA"));
        c.connect(c.getGate("SUM"), extra);
        List<Gate> second = c.getTopologicalOrder();
        assertNotSame(first, second, "changing the circuit must discard the cache");
        assertEquals(first.size() + 1, second.size());
        assertTrue(second.contains(extra));
        assertValidOrder(c);
    }

    @Test void returnedOrderCannotBeModified() {
        Circuit c = AdderGenerator.halfAdder();
        List<Gate> order = c.getTopologicalOrder();
        assertThrows(UnsupportedOperationException.class, () -> order.add(new NotGate("X")));
    }

    @Test void emptyCircuitHasEmptyOrder() {
        assertTrue(new Circuit().getTopologicalOrder().isEmpty());
    }

    @Test void sameSourceWiredTwiceStillOrders() {
        Circuit c = new Circuit();
        InputGate a = c.add(new InputGate("A"));
        Gate and = c.add(new AndGate("G"));
        c.connect(a, and);
        c.connect(a, and);                     // two wires from the same source
        List<Gate> order = c.getTopologicalOrder();
        assertEquals(2, order.size());
        assertSame(a, order.get(0));
        assertSame(and, order.get(1));
    }
}