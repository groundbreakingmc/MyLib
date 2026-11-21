package com.github.groundbreakingmc.mylib.collections.circular;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class CircularListTest {

    private CircularList<String> list;

    @BeforeEach
    void setUp() {
        this.list = new CircularList<>();
    }

    @Test
    @DisplayName("Create empty list with default constructor")
    void testEmptyList() {
        assertEquals(0, this.list.size());
        assertTrue(this.list.isEmpty());
    }

    @Test
    @DisplayName("Add elements and verify with get")
    void testAddAndGet() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");

        assertEquals(3, this.list.size());
        assertEquals("A", this.list.get(0));
        assertEquals("B", this.list.get(1));
        assertEquals("C", this.list.get(2));
    }

    @Test
    @DisplayName("Set element at index and return old value")
    void testSet() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");

        String old = this.list.set(1, "X");
        assertEquals("B", old);
        assertEquals("X", this.list.get(1));
    }

    @Test
    @DisplayName("Remove element by index")
    void testRemove() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");

        String removed = this.list.remove(1);
        assertEquals("B", removed);
        assertEquals(2, this.list.size());
        assertEquals("A", this.list.get(0));
        assertEquals("C", this.list.get(1));
    }

    @Test
    @DisplayName("Remove element by object reference")
    void testRemoveByObject() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");

        assertTrue(this.list.remove("B"));
        assertEquals(2, this.list.size());
        assertFalse(this.list.remove("X"));
    }

    @Test
    @DisplayName("Clear all elements from list")
    void testClear() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");

        this.list.clear();
        assertEquals(0, this.list.size());
        assertTrue(this.list.isEmpty());
    }

    @Test
    @DisplayName("Create list with initial capacity")
    void testConstructorWithCapacity() {
        CircularList<Integer> list = new CircularList<>(5);
        assertEquals(0, list.size());

        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        assertEquals(10, list.size());
    }

    @Test
    @DisplayName("Throw exception when maxSize is invalid")
    void testConstructorInvalidMaxSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CircularList<>(10, 0);
        });
    }

    @Test
    @DisplayName("Overwrite oldest element when maxSize is reached")
    void testCircularBehaviorBasic() {
        CircularList<Integer> list = new CircularList<>(3, 3);

        list.add(1);
        list.add(2);
        list.add(3);
        assertEquals(3, list.size());
        assertEquals(Arrays.asList(1, 2, 3), Arrays.asList(list.toArray()));

        list.add(4);
        assertEquals(3, list.size());
        assertEquals(Arrays.asList(2, 3, 4), Arrays.asList(list.toArray()));

        list.add(5);
        assertEquals(3, list.size());
        assertEquals(Arrays.asList(3, 4, 5), Arrays.asList(list.toArray()));
    }

    @Test
    @DisplayName("Handle multiple rotations in circular mode")
    void testCircularBehaviorMultipleRotations() {
        CircularList<Integer> list = new CircularList<>(3, 3);

        for (int i = 0; i < 10; i++) {
            list.add(i);
        }

        assertEquals(3, list.size());
        assertEquals(Arrays.asList(7, 8, 9), Arrays.asList(list.toArray()));
    }

    @Test
    @DisplayName("Support remove operations in circular mode")
    void testCircularBehaviorWithRemove() {
        CircularList<Integer> list = new CircularList<>(3, 5);

        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);

        assertEquals(5, list.size());

        list.add(6);
        assertEquals(5, list.size());
        assertEquals(Arrays.asList(2, 3, 4, 5, 6), Arrays.asList(list.toArray()));

        list.remove(0);
        assertEquals(4, list.size());
        assertEquals(Arrays.asList(3, 4, 5, 6), Arrays.asList(list.toArray()));

        list.add(7);
        assertEquals(5, list.size());
        assertEquals(Arrays.asList(3, 4, 5, 6, 7), Arrays.asList(list.toArray()));
    }

    @Test
    @DisplayName("Insert element at specific index")
    void testAddByIndex() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("D");

        this.list.add(2, "C");
        assertEquals(4, this.list.size());
        assertEquals(Arrays.asList("A", "B", "C", "D"), Arrays.asList(this.list.toArray()));
    }

    @Test
    @DisplayName("Insert element at the beginning")
    void testAddByIndexAtBeginning() {
        this.list.add("B");
        this.list.add("C");

        this.list.add(0, "A");
        assertEquals(3, this.list.size());
        assertEquals(Arrays.asList("A", "B", "C"), Arrays.asList(this.list.toArray()));
    }

    @Test
    @DisplayName("Insert element at the end using index")
    void testAddByIndexAtEnd() {
        this.list.add("A");
        this.list.add("B");

        this.list.add(2, "C");
        assertEquals(3, this.list.size());
        assertEquals(Arrays.asList("A", "B", "C"), Arrays.asList(this.list.toArray()));
    }

    @Test
    @DisplayName("Allow insertion at index when maxSize is reached")
    void testAddByIndexInCircularMode() {
        CircularList<Integer> list = new CircularList<>(3, 3);
        list.add(1);
        list.add(2);
        list.add(3);

        list.add(0, 0);
        assertEquals(3, list.size());
        assertEquals(Arrays.asList(0, 1, 2), Arrays.asList(list.toArray()));

        list.add(1, 10);
        assertEquals(3, list.size());
        assertEquals(Arrays.asList(0, 10, 1), Arrays.asList(list.toArray()));

        list.add(2, 99);
        assertEquals(3, list.size());
        assertEquals(Arrays.asList(0, 10, 99), Arrays.asList(list.toArray()));
    }

    @Test
    @DisplayName("Throw exception when inserting at invalid index")
    void testAddByIndexOutOfBounds() {
        this.list.add("A");

        assertThrows(IndexOutOfBoundsException.class, () -> this.list.add(-1, "X"));
        assertThrows(IndexOutOfBoundsException.class, () -> this.list.add(5, "X"));
    }

    @Test
    @DisplayName("Find first occurrence of element")
    void testIndexOf() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");
        this.list.add("B");

        assertEquals(0, this.list.indexOf("A"));
        assertEquals(1, this.list.indexOf("B"));
        assertEquals(2, this.list.indexOf("C"));
        assertEquals(-1, this.list.indexOf("X"));
    }

    @Test
    @DisplayName("Find last occurrence of element")
    void testLastIndexOf() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");
        this.list.add("B");

        assertEquals(0, this.list.lastIndexOf("A"));
        assertEquals(3, this.list.lastIndexOf("B"));
        assertEquals(2, this.list.lastIndexOf("C"));
        assertEquals(-1, this.list.lastIndexOf("X"));
    }

    @Test
    @DisplayName("Find null elements with indexOf and lastIndexOf")
    void testIndexOfWithNull() {
        this.list.add("A");
        this.list.add(null);
        this.list.add("C");

        assertEquals(1, this.list.indexOf(null));
        assertEquals(1, this.list.lastIndexOf(null));
    }

    @Test
    @DisplayName("Check if list contains element")
    void testContains() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");

        assertTrue(this.list.contains("A"));
        assertTrue(this.list.contains("B"));
        assertFalse(this.list.contains("X"));
    }

    @Test
    @DisplayName("Check if list contains null element")
    void testContainsNull() {
        this.list.add("A");
        this.list.add(null);
        this.list.add("C");

        assertTrue(this.list.contains(null));
    }

    @Test
    @DisplayName("Convert list to Object array")
    void testToArray() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");

        Object[] array = this.list.toArray();
        assertArrayEquals(new Object[]{"A", "B", "C"}, array);
    }

    @Test
    @DisplayName("Convert list to typed array")
    void testToArrayWithParameter() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");

        String[] array = this.list.toArray(new String[0]);
        assertArrayEquals(new String[]{"A", "B", "C"}, array);

        String[] largeArray = new String[5];
        String[] result = this.list.toArray(largeArray);
        assertSame(largeArray, result);
        assertEquals("A", result[0]);
        assertEquals("B", result[1]);
        assertEquals("C", result[2]);
        assertNull(result[3]);
    }

    @Test
    @DisplayName("Convert to array after circular wrap")
    void testToArrayAfterCircularWrap() {
        CircularList<Integer> list = new CircularList<>(3, 3);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);

        Object[] array = list.toArray();
        assertArrayEquals(new Object[]{3, 4, 5}, array);
    }

    @Test
    @DisplayName("Throw exception when getting element at invalid index")
    void testGetOutOfBounds() {
        this.list.add("A");

        assertThrows(IndexOutOfBoundsException.class, () -> this.list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> this.list.get(1));
    }

    @Test
    @DisplayName("Throw exception when setting element at invalid index")
    void testSetOutOfBounds() {
        this.list.add("A");

        assertThrows(IndexOutOfBoundsException.class, () -> this.list.set(-1, "X"));
        assertThrows(IndexOutOfBoundsException.class, () -> this.list.set(1, "X"));
    }

    @Test
    @DisplayName("Throw exception when removing element at invalid index")
    void testRemoveOutOfBounds() {
        this.list.add("A");

        assertThrows(IndexOutOfBoundsException.class, () -> this.list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> this.list.remove(1));
    }

    @Test
    @DisplayName("Throw exception when removing from empty list")
    void testRemoveFromEmptyList() {
        assertThrows(IndexOutOfBoundsException.class, () -> this.list.remove(0));
    }

    @Test
    @DisplayName("Automatically grow beyond initial capacity")
    void testGrowBeyondInitialCapacity() {
        CircularList<Integer> list = new CircularList<>(2);

        for (int i = 0; i < 20; i++) {
            list.add(i);
        }

        assertEquals(20, list.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(i, list.get(i));
        }
    }

    @Test
    @DisplayName("Perform mixed add, insert, and remove operations")
    void testMixedOperations() {
        CircularList<Integer> list = new CircularList<>(3, 5);

        list.add(1);
        list.add(2);
        list.add(3);
        assertEquals(Arrays.asList(1, 2, 3), Arrays.asList(list.toArray()));

        list.add(1, 10);
        assertEquals(Arrays.asList(1, 10, 2, 3), Arrays.asList(list.toArray()));

        list.remove(0);
        assertEquals(Arrays.asList(10, 2, 3), Arrays.asList(list.toArray()));

        list.add(4);
        list.add(5);
        assertEquals(Arrays.asList(10, 2, 3, 4, 5), Arrays.asList(list.toArray()));

        list.add(6);
        assertEquals(Arrays.asList(2, 3, 4, 5, 6), Arrays.asList(list.toArray()));
    }

    @Test
    @DisplayName("Support null elements in all operations")
    void testNullElements() {
        this.list.add(null);
        this.list.add("A");
        this.list.add(null);
        this.list.add("B");

        assertEquals(4, this.list.size());
        assertNull(this.list.get(0));
        assertEquals("A", this.list.get(1));
        assertNull(this.list.get(2));
        assertEquals("B", this.list.get(3));

        assertEquals(0, this.list.indexOf(null));
        assertEquals(2, this.list.lastIndexOf(null));
        assertTrue(this.list.remove(null));
        assertEquals(3, this.list.size());
    }

    @Test
    @DisplayName("Work correctly with empty list")
    void testEmptyListOperations() {
        assertTrue(this.list.isEmpty());
        assertEquals(0, this.list.size());
        assertFalse(this.list.contains("anything"));
        assertEquals(-1, this.list.indexOf("anything"));
        assertArrayEquals(new Object[0], this.list.toArray());
    }

    @Test
    @DisplayName("Handle single element correctly")
    void testSingleElement() {
        this.list.add("only");

        assertEquals(1, this.list.size());
        assertEquals("only", this.list.get(0));
        assertTrue(this.list.contains("only"));

        this.list.remove(0);
        assertTrue(this.list.isEmpty());
    }

    @Test
    @DisplayName("Perform many sequential operations")
    void testManyOperations() {
        for (int i = 0; i < 100; i++) {
            this.list.add("element" + i);
        }

        assertEquals(100, this.list.size());

        for (int i = 0; i < 50; i++) {
            this.list.remove(0);
        }

        assertEquals(50, this.list.size());
        assertEquals("element50", this.list.get(0));
    }

    @Test
    @DisplayName("Clear list and verify it is empty")
    void testClearEmptiesList() {
        this.list.add("A");
        this.list.add("B");

        this.list.clear();
        assertEquals(0, this.list.size());

        this.list.clear();
        assertEquals(0, this.list.size());
    }

    @Test
    @DisplayName("Iterator works with foreach")
    void testIteratorForEach() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");

        final StringBuilder sb = new StringBuilder();
        for (String s : this.list) {
            sb.append(s);
        }

        assertEquals("ABC", sb.toString());
    }

    @Test
    @DisplayName("Iterator respects circular order after overflow")
    void testIteratorCircularOrder() {
        final CircularList<Integer> list = new CircularList<>(3, 3);

        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);

        Iterator<Integer> it = list.iterator();

        assertTrue(it.hasNext());
        assertEquals(3, it.next());
        assertEquals(4, it.next());
        assertEquals(5, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    @DisplayName("Iterator remove removes last returned element")
    void testIteratorRemove() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");

        final Iterator<String> it = this.list.iterator();

        assertEquals("A", it.next());
        it.remove();

        assertEquals(2, this.list.size());
        assertEquals(Arrays.asList("B", "C"), Arrays.asList(this.list.toArray()));
    }

    @Test
    @DisplayName("Iterator iterates elements in insertion order")
    void testIteratorNormalOrder() {
        this.list.add("A");
        this.list.add("B");
        this.list.add("C");

        final Iterator<String> it = this.list.iterator();

        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertTrue(it.hasNext());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    @DisplayName("Iterator on empty list")
    void testIteratorEmpty() {
        final Iterator<String> it = this.list.iterator();

        assertNotNull(it);
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }
}
