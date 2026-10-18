package gates;

public class XnorGate extends Gate {
    public XnorGate(String name) { super(name); }

    @Override
    protected boolean compute() {
        boolean result = false;
        for (int i = 0; i < inputCount(); i++) {
            result ^= in(i);
        }
        return !result;
    }
}