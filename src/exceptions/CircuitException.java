package exceptions;

public class CircuitException extends RuntimeException {
    public CircuitException(String message) {
        super(message);
    }
}