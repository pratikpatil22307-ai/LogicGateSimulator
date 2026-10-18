package engine;

import java.util.NoSuchElementException;
import java.util.PriorityQueue;

/**
 * The engine's to-do list: positions of gates waiting to be re-evaluated.
 * poll() always returns the smallest waiting position, which is the earliest gate
 * in topological order. A position can be waiting at most once.
 */
public final class EventQueue {

    private final PriorityQueue<Integer> heap = new PriorityQueue<>();
    private final boolean[] waiting;

    public EventQueue(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity cannot be negative");
        }
        waiting = new boolean[capacity];
    }

    /** Adds a position. Does nothing if it is already waiting. Returns true if it was added. */
    public boolean schedule(int position) {
        if (position < 0 || position >= waiting.length) {
            throw new IllegalArgumentException("position out of range: " + position);
        }
        if (waiting[position]) {
            return false;
        }
        waiting[position] = true;
        heap.add(position);
        return true;
    }

    /** Removes and returns the smallest waiting position. */
    public int poll() {
        Integer position = heap.poll();
        if (position == null) {
            throw new NoSuchElementException("queue is empty");
        }
        waiting[position] = false;
        return position;
    }

    public boolean isEmpty() { return heap.isEmpty(); }
    public int size() { return heap.size(); }
}