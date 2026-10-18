package gates;

import java.util.Objects;

public class GateFactory {

    private GateFactory() { }

    public static Gate create(GateType type, String name) {
        Objects.requireNonNull(type, "type cannot be null");
        return switch (type) {
            case INPUT -> new InputGate(name);
            case AND   -> new AndGate(name);
            case OR    -> new OrGate(name);
            case NOT   -> new NotGate(name);
            case NAND  -> new NandGate(name);
            case NOR   -> new NorGate(name);
            case XOR   -> new XorGate(name);
            case XNOR  -> new XnorGate(name);
        };
    }
}