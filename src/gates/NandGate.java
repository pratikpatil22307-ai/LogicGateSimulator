package gates;

public class NandGate extends Gate {
    public NandGate(String name) { super(name); }

    @Override
    protected boolean compute() {
        for (int i = 0; i < inputCount(); i++) {
            if (!in(i)) return true;
        }
        return false;
    }
}