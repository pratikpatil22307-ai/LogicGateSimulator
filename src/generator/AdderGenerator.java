package generator;

import java.math.BigInteger;

import circuit.Circuit;
import gates.AndGate;
import gates.Gate;
import gates.InputGate;
import gates.OrGate;
import gates.XorGate;
import util.BitUtil;

public final class AdderGenerator {

    private AdderGenerator() { }

    /** Inputs: A, B. Outputs: SUM, CARRY. */
    public static Circuit halfAdder() {
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

    /** Inputs: A, B, CIN. Outputs: SUM, COUT. */
    public static Circuit fullAdder() {
        Circuit c = new Circuit();
        InputGate a = c.add(new InputGate("A"));
        InputGate b = c.add(new InputGate("B"));
        InputGate cin = c.add(new InputGate("CIN"));

        Gate x1 = c.add(new XorGate("X1"));      // A xor B
        Gate sum = c.add(new XorGate("SUM"));    // (A xor B) xor CIN
        Gate a1 = c.add(new AndGate("A1"));      // A and B
        Gate a2 = c.add(new AndGate("A2"));      // (A xor B) and CIN
        Gate cout = c.add(new OrGate("COUT"));   // A1 or A2

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

    /**
     * Builds a ripple-carry adder.
     * Inputs:  A0..A(width-1), B0..B(width-1), CIN   (2*width + 1 values)
     * Outputs: S0..S(width-1), COUT                  (width + 1 values)
     */
    public static Circuit rippleCarry(int width) {
        if (width < 1) {
            throw new IllegalArgumentException("width must be at least 1");
        }
        Circuit c = new Circuit();

        InputGate[] a = new InputGate[width];
        InputGate[] b = new InputGate[width];
        for (int i = 0; i < width; i++) {
            a[i] = c.add(new InputGate("A" + i));
        }
        for (int i = 0; i < width; i++) {
            b[i] = c.add(new InputGate("B" + i));
        }
        InputGate cin = c.add(new InputGate("CIN"));

        Gate carry = cin;   // the carry coming into the current bit
        for (int i = 0; i < width; i++) {
            Gate x = c.add(new XorGate("X" + i));      // A xor B
            Gate sum = c.add(new XorGate("S" + i));    // (A xor B) xor carry-in
            Gate g = c.add(new AndGate("G" + i));      // A and B
            Gate p = c.add(new AndGate("P" + i));      // (A xor B) and carry-in
            String carryName = (i == width - 1) ? "COUT" : "C" + (i + 1);
            Gate next = c.add(new OrGate(carryName));  // carry-out of this bit

            c.connect(a[i], x);
            c.connect(b[i], x);
            c.connect(x, sum);
            c.connect(carry, sum);
            c.connect(a[i], g);
            c.connect(b[i], g);
            c.connect(x, p);
            c.connect(carry, p);
            c.connect(g, next);
            c.connect(p, next);

            c.markOutput(sum);
            carry = next;
        }
        c.markOutput(carry);   // COUT goes last
        return c;
    }

    /** Builds the input array for rippleCarry(width): A bits, then B bits, then CIN. */
    public static boolean[] packInputs(BigInteger a, BigInteger b, boolean cin, int width) {
        boolean[] in = new boolean[2 * width + 1];
        System.arraycopy(BitUtil.toBits(a, width), 0, in, 0, width);
        System.arraycopy(BitUtil.toBits(b, width), 0, in, width, width);
        in[2 * width] = cin;
        return in;
    }

    /** Turns the outputs of rippleCarry(width) into one number (sum bits plus the carry as the top bit). */
    public static BigInteger unpackSum(boolean[] outputs, int width) {
        if (outputs.length != width + 1) {
            throw new IllegalArgumentException("expected " + (width + 1) + " outputs, got " + outputs.length);
        }
        return BitUtil.toBigInteger(outputs, 0, width + 1);
    }
}