package gates;

public class AndGate extends Gate {
    public AndGate(String name) { super(name); }

    @Override
    protected boolean compute() {
        for (int i = 0; i < inputCount(); i++) {
            if (!in(i)) return false;
        }
        return true;
    }
}