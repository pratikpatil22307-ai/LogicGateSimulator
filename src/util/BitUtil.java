package util;

import java.math.BigInteger;

/** Converts between numbers and bit arrays. Index 0 is the least significant bit. */
public final class BitUtil {

    private BitUtil() { }

    public static boolean[] toBits(long value, int width) {
        if (width < 1 || width > 63) {
            throw new IllegalArgumentException("width must be between 1 and 63");
        }
        if (value < 0 || value >= (1L << width)) {
            throw new IllegalArgumentException("value " + value + " does not fit in " + width + " bits");
        }
        boolean[] bits = new boolean[width];
        for (int i = 0; i < width; i++) {
            bits[i] = ((value >> i) & 1L) != 0;
        }
        return bits;
    }

    public static long toLong(boolean[] bits, int from, int length) {
        if (from < 0 || length < 0 || length > 63 || from + length > bits.length) {
            throw new IllegalArgumentException("invalid slice");
        }
        long value = 0;
        for (int i = 0; i < length; i++) {
            if (bits[from + i]) {
                value |= 1L << i;
            }
        }
        return value;
    }

    /** For widths beyond 63 bits, where long is too small. */
    public static boolean[] toBits(BigInteger value, int width) {
        if (width < 1) {
            throw new IllegalArgumentException("width must be at least 1");
        }
        if (value.signum() < 0 || value.bitLength() > width) {
            throw new IllegalArgumentException("value does not fit in " + width + " bits");
        }
        boolean[] bits = new boolean[width];
        for (int i = 0; i < width; i++) {
            bits[i] = value.testBit(i);
        }
        return bits;
    }

    public static BigInteger toBigInteger(boolean[] bits, int from, int length) {
        if (from < 0 || length < 0 || from + length > bits.length) {
            throw new IllegalArgumentException("invalid slice");
        }
        BigInteger value = BigInteger.ZERO;
        for (int i = 0; i < length; i++) {
            if (bits[from + i]) {
                value = value.setBit(i);
            }
        }
        return value;
    }
}