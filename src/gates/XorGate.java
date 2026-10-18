package gates;

public class XorGate extends Gate {
    public XorGate(String name) { super(name); }

    @Override
    protected boolean compute() {
        boolean result = false;
        for (int i = 0; i < inputCount(); i++) {
            result ^= in(i);
        }
        return result;
    }
}