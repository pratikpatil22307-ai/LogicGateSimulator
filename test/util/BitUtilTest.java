package util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.Random;

import org.junit.jupiter.api.Test;

class BitUtilTest {

    @Test void toBitsIsLeastSignificantFirst() {
        // 5 is binary 0101, written lowest bit first
        assertArrayEquals(new boolean[] { true, false, true, false }, BitUtil.toBits(5L, 4));
    }

    @Test void longRoundTrip() {
        for (long v = 0; v < 256; v++) {
            assertEquals(v, BitUtil.toLong(BitUtil.toBits(v, 8), 0, 8));
        }
    }

    @Test void toLongReadsASlice() {
        boolean[] bits = { true, true, false, true };
        // bits[1], bits[2], bits[3] = 1, 0, 1 -> 1 + 0 + 4 = 5
        assertEquals(5L, BitUtil.toLong(bits, 1, 3));
    }

    @Test void valueTooBigRejected() {
        assertThrows(IllegalArgumentException.class, () -> BitUtil.toBits(16L, 4));
    }

    @Test void negativeValueRejected() {
        assertThrows(IllegalArgumentException.class, () -> BitUtil.toBits(-1L, 4));
    }

    @Test void bigIntegerRoundTrip() {
        Random rnd = new Random(1);
        for (int i = 0; i < 20; i++) {
            BigInteger v = new BigInteger(100, rnd);
            assertEquals(v, BitUtil.toBigInteger(BitUtil.toBits(v, 100), 0, 100));
        }
    }

    @Test void bigIntegerTooWideRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> BitUtil.toBits(BigInteger.ONE.shiftLeft(10), 10));
    }
}