package generator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import circuit.Circuit;

class ChainGeneratorTest {

    @Test void shape() {
        Circuit c = ChainGenerator.notChain(10, false);
        assertEquals(11, c.size());
        assertEquals(1, c.getInputs().size());
        assertEquals(1, c.getOutputs().size());
        assertDoesNotThrow(c::validate);
        assertDoesNotThrow(c::getTopologicalOrder);
    }

    @Test void reverseFlagChangesInsertionOrder() {
        assertEquals("IN", ChainGenerator.notChain(10, false).getGates().get(0).getName());
        assertEquals("N10", ChainGenerator.notChain(10, true).getGates().get(0).getName());
        assertEquals("IN", ChainGenerator.notChain(10, true).getGates().get(10).getName());
    }

    @Test void zeroLengthRejected() {
        assertThrows(IllegalArgumentException.class, () -> ChainGenerator.notChain(0, false));
    }
}