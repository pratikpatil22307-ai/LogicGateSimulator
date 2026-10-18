package engine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.jupiter.api.Test;

class EventQueueTest {

    @Test void startsEmpty() {
        EventQueue q = new EventQueue(10);
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
    }

    @Test void pollsSmallestFirstRegardlessOfInsertionOrder() {
        EventQueue q = new EventQueue(10);
        q.schedule(5);
        q.schedule(1);
        q.schedule(3);
        assertEquals(1, q.poll());
        assertEquals(3, q.poll());
        assertEquals(5, q.poll());
        assertTrue(q.isEmpty());
    }

    @Test void schedulingTwiceKeepsOneCopy() {
        EventQueue q = new EventQueue(10);
        assertTrue(q.schedule(2));
        assertFalse(q.schedule(2));
        assertEquals(1, q.size());
        assertEquals(2, q.poll());
        assertTrue(q.isEmpty());
    }

    @Test void canBeScheduledAgainAfterPolling() {
        EventQueue q = new EventQueue(10);
        q.schedule(4);
        q.poll();
        assertTrue(q.schedule(4));
        assertEquals(4, q.poll());
    }

    @Test void outOfRangePositionsRejected() {
        EventQueue q = new EventQueue(5);
        assertThrows(IllegalArgumentException.class, () -> q.schedule(-1));
        assertThrows(IllegalArgumentException.class, () -> q.schedule(5));
    }

    @Test void pollOnEmptyQueueThrows() {
        assertThrows(NoSuchElementException.class, () -> new EventQueue(3).poll());
    }

    @Test void negativeCapacityRejected() {
        assertThrows(IllegalArgumentException.class, () -> new EventQueue(-1));
    }

    @Test void manyPositionsComeOutSorted() {
        int n = 1000;
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            positions.add(i);
        }
        Collections.shuffle(positions, new Random(5));

        EventQueue q = new EventQueue(n);
        for (int p : positions) {
            q.schedule(p);
        }
        for (int expected = 0; expected < n; expected++) {
            assertEquals(expected, q.poll());
        }
        assertTrue(q.isEmpty());
    }
}