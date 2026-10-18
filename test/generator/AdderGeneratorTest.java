package generator;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.Random;

import org.junit.jupiter.api.Test;

import circuit.Circuit;
import engine.NaiveEngine;
import engine.SimulationEngine;

class AdderGeneratorTest {

    private final SimulationEngine engine = new NaiveEngine();

    private BigInteger add(Circuit adder, int width, BigInteger a, BigInteger b, boolean cin) {
        boolean[] out = engine.simulate(adder, AdderGenerator.packInputs(a, b, cin, width));
        return AdderGenerator.unpackSum(out, width);
    }

    @Test void sizesAreAsDesigned() {
        for (int w : new int[] { 1, 4, 16, 64 }) {
            Circuit c = AdderGenerator.rippleCarry(w);
            assertEquals(7 * w + 1, c.size(), "gates, width " + w);
            assertEquals(2 * w + 1, c.getInputs().size(), "inputs, width " + w);
            assertEquals(w + 1, c.getOutputs().size(), "outputs, width " + w);
            assertDoesNotThrow(c::validate);
        }
    }

    @Test void fourBitAdderExhaustive() {
        Circuit c = AdderGenerator.rippleCarry(4);
        for (int a = 0; a < 16; a++) {
            for (int b = 0; b < 16; b++) {
                for (int cin = 0; cin < 2; cin++) {
                    BigInteger got = add(c, 4, BigInteger.valueOf(a), BigInteger.valueOf(b), cin == 1);
                    assertEquals(BigInteger.valueOf(a + b + cin), got, a + " + " + b + " + " + cin);
                }
            }
        }
    }

    @Test void sixteenBitAdderMatchesJavaOnRandomPairs() {
        Circuit c = AdderGenerator.rippleCarry(16);
        Random rnd = new Random(42);
        for (int i = 0; i < 1000; i++) {
            int a = rnd.nextInt(1 << 16);
            int b = rnd.nextInt(1 << 16);
            boolean cin = rnd.nextBoolean();
            BigInteger expected = BigInteger.valueOf((long) a + b + (cin ? 1 : 0));
            assertEquals(expected, add(c, 16, BigInteger.valueOf(a), BigInteger.valueOf(b), cin),
                    a + " + " + b + " + " + cin);
        }
    }

    @Test void sixteenBitEdgeCases() {
        Circuit c = AdderGenerator.rippleCarry(16);
        BigInteger max = BigInteger.valueOf(65535);
        assertEquals(BigInteger.valueOf(65536), add(c, 16, max, BigInteger.ZERO, true));   // carry ripples all the way
        assertEquals(BigInteger.valueOf(131071), add(c, 16, max, max, true));              // every output bit is 1
        assertEquals(BigInteger.ZERO, add(c, 16, BigInteger.ZERO, BigInteger.ZERO, false));
    }

    @Test void thousandTwentyFourBitAdderMatchesBigInteger() {
        int width = 1024;
        Circuit c = AdderGenerator.rippleCarry(width);
        Random rnd = new Random(7);
        for (int i = 0; i < 10; i++) {
            BigInteger a = new BigInteger(width, rnd);
            BigInteger b = new BigInteger(width, rnd);
            boolean cin = rnd.nextBoolean();
            BigInteger expected = a.add(b).add(cin ? BigInteger.ONE : BigInteger.ZERO);
            assertEquals(expected, add(c, width, a, b, cin));
        }
    }

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

    @Test void fullAdderMatchesArithmetic() {
        Circuit c = AdderGenerator.fullAdder();
        for (int r = 0; r < 8; r++) {
            int a = (r >> 2) & 1;
            int b = (r >> 1) & 1;
            int cin = r & 1;
            boolean[] out = engine.simulate(c, new boolean[] { a == 1, b == 1, cin == 1 });
            assertEquals((a + b + cin) % 2 == 1, out[0], "SUM row " + r);
            assertEquals(a + b + cin >= 2, out[1], "COUT row " + r);
        }
    }

    @Test void packInputsLayout() {
        boolean[] in = AdderGenerator.packInputs(BigInteger.ONE, BigInteger.TWO, true, 4);
        boolean[] expected = {
            true, false, false, false,    // A = 1
            false, true, false, false,    // B = 2
            true                          // CIN
        };
        assertArrayEquals(expected, in);
    }

    @Test void unpackSumRejectsWrongLength() {
        assertThrows(IllegalArgumentException.class, () -> AdderGenerator.unpackSum(new boolean[3], 4));
    }

    @Test void zeroWidthRejected() {
        assertThrows(IllegalArgumentException.class, () -> AdderGenerator.rippleCarry(0));
    }
}