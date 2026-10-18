package exceptions;

/** Thrown when a circuit contains a feedback loop, so no valid evaluation order exists. */
public class CycleDetectedException extends CircuitException {
    public CycleDetectedException(String message) {
        super(message);
    }
}