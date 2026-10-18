package gates;

public class NotGate extends Gate {
    public NotGate(String name) { super(name); }
    
    @Override public int minInputs() { return 1; }
    @Override public int maxInputs() { return 1; }

    @Override
    protected boolean compute() { return !in(0); }
}