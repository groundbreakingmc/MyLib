package com.github.groundbreakingmc.mylib.collections.сircular;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircularListTest {

    private CircularList<String> list;

    @BeforeEach
    void setUp() {
        list = new CircularList<>();
    }

    @Test
    @DisplayName("Create empty list with default constructor")
    void testDefaultConstructor() {
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("Create list with initial capacity")
    void testConstructorWithCapacity() {
        CircularList<Integer> list = new CircularList<>(20);
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("Create list with maximum size")
    void testConstructorWithMaxSize() {
        CircularList<Integer> list = new CircularList<>(10, 50);
        assertEquals(0, list.size());
    }

    @Test
    @DisplayName("Add element to the end")
    void testAdd() {
        assertTrue(list.add("first"));
        assertEquals(1, list.size());
        assertEquals("first", list.get(0));
    }

    @Test
    @DisplayName("Add multiple elements")
    void testAddMultiple() {
        list.add("first");
        list.add("second");
        list.add("third");

        assertEquals(3, list.size());
        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
        assertEquals("third", list.get(2));
    }

    @Test
    @DisplayName("Add element at specific index")
    void testAddAtIndex() {
        list.add("first");
        list.add("third");
        list.add(1, "second");

        assertEquals(3, list.size());
        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
        assertEquals("third", list.get(2));
    }

    @Test
    @DisplayName("Add element at the beginning")
    void testAddAtBeginning() {
        list.add("second");
        list.add("third");
        list.add(0, "first");

        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
        assertEquals("third", list.get(2));
    }

    @Test
    @DisplayName("Add element at the end using index")
    void testAddAtEnd() {
        list.add("first");
        list.add("second");
        list.add(2, "third");

        assertEquals(3, list.size());
        assertEquals("third", list.get(2));
    }

    @Test
    @DisplayName("Throw exception when adding at invalid index")
    void testAddAtInvalidIndex() {
        list.add("first");

        assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, "invalid"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(5, "invalid"));
    }

    @Test
    @DisplayName("Remove element by index")
    void testRemoveByIndex() {
        list.add("first");
        list.add("second");
        list.add("third");

        String removed = list.remove(1);

        assertEquals("second", removed);
        assertEquals(2, list.size());
        assertEquals("first", list.get(0));
        assertEquals("third", list.get(1));
    }

    @Test
    @DisplayName("Remove element by value")
    void testRemoveByValue() {
        list.add("first");
        list.add("second");
        list.add("third");

        assertTrue(list.remove("second"));
        assertEquals(2, list.size());
        assertFalse(list.contains("second"));
    }

    @Test
    @DisplayName("Remove non-existent element")
    void testRemoveNonExistent() {
        list.add("first");
        assertFalse(list.remove("nonexistent"));
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Remove null element")
    void testRemoveNull() {
        list.add("first");
        list.add(null);
        list.add("third");

        assertTrue(list.remove(null));
        assertEquals(2, list.size());
        assertFalse(list.contains(null));
    }

    @Test
    @DisplayName("Throw exception when removing at invalid index")
    void testRemoveInvalidIndex() {
        list.add("first");

        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(5));
    }

    @Test
    @DisplayName("Get element by index")
    void testGet() {
        list.add("first");
        list.add("second");

        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
    }

    @Test
    @DisplayName("Set element at index")
    void testSet() {
        list.add("first");
        list.add("second");

        String old = list.set(1, "updated");

        assertEquals("second", old);
        assertEquals("updated", list.get(1));
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("Throw exception when getting at invalid index")
    void testGetInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    }

    @Test
    @DisplayName("Find index of element")
    void testIndexOf() {
        list.add("first");
        list.add("second");
        list.add("third");

        assertTrue(list.indexOf("second") >= 0);
        assertEquals(-1, list.indexOf("nonexistent"));
    }

    @Test
    @DisplayName("Find last index of element")
    void testLastIndexOf() {
        list.add("first");
        list.add("second");
        list.add("first");

        int lastIndex = list.lastIndexOf("first");
        assertTrue(lastIndex >= 0);
    }

    @Test
    @DisplayName("Find index of null element")
    void testIndexOfNull() {
        list.add("first");
        list.add(null);
        list.add("third");

        assertTrue(list.indexOf(null) >= 0);
    }

    @Test
    @DisplayName("Check if list contains element")
    void testContains() {
        list.add("first");
        list.add("second");

        assertTrue(list.contains("first"));
        assertTrue(list.contains("second"));
        assertFalse(list.contains("third"));
    }

    @Test
    @DisplayName("Check if list contains null")
    void testContainsNull() {
        list.add("first");
        list.add(null);

        assertTrue(list.contains(null));
    }


    @Test
    @DisplayName("Clear the list")
    void testClear() {
        list.add("first");
        list.add("second");
        list.add("third");

        list.clear();

        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("Convert to Object array")
    void testToArray() {
        list.add("first");
        list.add("second");
        list.add("third");

        Object[] array = list.toArray();

        assertEquals(3, array.length);
        assertEquals("first", array[0]);
        assertEquals("second", array[1]);
        assertEquals("third", array[2]);
    }

    @Test
    @DisplayName("Convert to typed array")
    void testToArrayTyped() {
        list.add("first");
        list.add("second");

        String[] array = list.toArray(new String[0]);

        assertEquals(2, array.length);
        assertEquals("first", array[0]);
        assertEquals("second", array[1]);
    }

    @Test
    @DisplayName("Convert to larger array")
    void testToArrayLarger() {
        list.add("first");
        list.add("second");

        String[] array = list.toArray(new String[5]);

        assertEquals(5, array.length);
        assertEquals("first", array[0]);
        assertEquals("second", array[1]);
        assertNull(array[2]);
    }

    @Test
    @DisplayName("Exceed maximum size")
    void testMaxSizeExceeded() {
        CircularList<Integer> limitedList = new CircularList<>(5, 10);

        for (int i = 0; i < 10; i++) {
            limitedList.add(i);
        }

        assertThrows(IllegalStateException.class, () -> limitedList.add(10));
    }

    @Test
    @DisplayName("Work with empty list")
    void testEmptyList() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertFalse(list.contains("anything"));
        assertEquals(-1, list.indexOf("anything"));
        assertArrayEquals(new Object[0], list.toArray());
    }

    @Test
    @DisplayName("Work with single element")
    void testSingleElement() {
        list.add("only");

        assertEquals(1, list.size());
        assertEquals("only", list.get(0));
        assertTrue(list.contains("only"));

        list.remove(0);
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("Perform multiple add and remove operations")
    void testMultipleOperations() {
        for (int i = 0; i < 100; i++) {
            list.add("element" + i);
        }

        assertEquals(100, list.size());

        for (int i = 0; i < 50; i++) {
            list.remove(0);
        }

        assertEquals(50, list.size());
        assertEquals("element50", list.get(0));
    }

    @Test
    @DisplayName("Work with null values")
    void testNullValues() {
        list.add(null);
        list.add("notNull");
        list.add(null);

        assertEquals(3, list.size());
        assertNull(list.get(0));
        assertEquals("notNull", list.get(1));
        assertNull(list.get(2));
    }

    @Test
    @DisplayName("Auto-grow capacity")
    void testAutoGrowth() {
        CircularList<Integer> smallList = new CircularList<>(2);

        for (int i = 0; i < 20; i++) {
            smallList.add(i);
        }

        assertEquals(20, smallList.size());
        assertEquals(0, smallList.get(0));
        assertEquals(19, smallList.get(19));
    }
}
