package gates;

public class OrGate extends Gate {
    public OrGate(String name) { super(name); }

    @Override
    protected boolean compute() {
        for (int i = 0; i < inputCount(); i++) {
            if (in(i)) return true;
        }
        return false;
    }
}