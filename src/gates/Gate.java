package gates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Gate {
    private final String name;
    private final List<Gate> inputs = new ArrayList<>();
    private final List<Gate> fanout = new ArrayList<>(); // gates that use my output
    private boolean output;

    public Gate(String name) {
        this.name = name;
    }

    public void addInput(Gate source) {
        if (source == null) throw new IllegalArgumentException("source cannot be null");
        if (source == this) throw new IllegalArgumentException("a gate cannot feed itself");
        inputs.add(source);
        source.fanout.add(this);
    }

    
    /** Minimum number of inputs this gate needs to be valid. */
    public int minInputs() { return 2; }

    /** Maximum number of inputs this gate can accept. */
    public int maxInputs() { return Integer.MAX_VALUE; }
    
    
    /** Recomputes the output. Returns true if the output changed. */
    public boolean update() {
        boolean old = output;
        output = compute();
        return old != output;
    }

    protected abstract boolean compute();

    protected boolean in(int i) { return inputs.get(i).output; }
    protected int inputCount() { return inputs.size(); }
    
    

    public String getName() { return name; }
    public boolean getOutput() { return output; }
    public List<Gate> getInputs() { return Collections.unmodifiableList(inputs); }
    public List<Gate> getFanout() { return Collections.unmodifiableList(fanout); }
}