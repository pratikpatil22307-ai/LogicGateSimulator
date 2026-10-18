package gates;

public class NorGate extends Gate {
    public NorGate(String name) { super(name); }

    @Override
    protected boolean compute() {
        for (int i = 0; i < inputCount(); i++) {
            if (in(i)) return false;
        }
        return true;
    }
}