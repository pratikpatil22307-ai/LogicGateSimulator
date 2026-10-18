package gates;

public class InputGate extends Gate {
    private boolean value;

    public InputGate(String name) { super(name); }
    
    @Override public int minInputs() { return 0; }
    @Override public int maxInputs() { return 0; }

    public void setValue(boolean value) { this.value = value; }
    public boolean getValue() { return value; }

    @Override
    public void addInput(Gate source) {
        throw new UnsupportedOperationException("An InputGate cannot have inputs");
    }

    @Override
    protected boolean compute() { return value; }
}