package com.github.groundbreakingmc.mylib.collections.circular;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;

/**
 * A resizable array implementation that uses a circular buffer internally.
 * This list stores elements in an array with a "head" pointer that allows
 * efficient rotation and wrapping without moving elements.
 *
 * <p>The circular nature of the internal storage means that the logical first
 * element (index 0) may not be stored at position 0 in the underlying array.
 * The {@code head} field tracks where the logical beginning is located.</p>
 *
 * <p>This implementation provides constant-time positional access and array
 * operations like get, set, add (at end), and size. Insert and remove operations
 * in the middle of the list have O(n) complexity due to element shifting.</p>
 *
 * <p>The list can optionally be created with a maximum size limit. When the
 * limit is reached, attempting to add more elements will overwrite the oldest
 * elements in a circular fashion, maintaining a constant size.</p>
 *
 * <p><strong>Note:</strong> This implementation is not synchronized. If multiple
 * threads access a CircularList instance concurrently, and at least one thread
 * modifies the list structurally, it must be synchronized externally.</p>
 *
 * @param <E> the type of elements in this list
 * @author GroundbreakingMC
 * @version 2.0
 */
public class CircularList<E> extends AbstractList<E> {

    private static final Object[] EMPTY = new Object[0];
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * The array buffer into which the elements of the CircularList are stored.
     * The capacity is the length of this array.
     */
    private E[] elements;

    /**
     * The index in the element array where the logical first element (index 0) is stored.
     * This allows the list to wrap around the array without moving elements.
     */
    private int head;

    /**
     * The number of elements currently in the list.
     */
    private int size;

    /**
     * The maximum number of elements this list can hold.
     * Set to Integer.MAX_VALUE for unlimited growth.
     */
    private final int maxSize;

    /**
     * Constructs an empty list with default initial capacity (10)
     * and no maximum size limit.
     */
    @SuppressWarnings("unchecked")
    public CircularList() {
        this.elements = (E[]) EMPTY;
        this.head = 0;
        this.size = 0;
        this.maxSize = Integer.MAX_VALUE;
    }

    /**
     * Constructs an empty list with the specified initial capacity
     * and no maximum size limit.
     *
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity is negative
     */
    @SuppressWarnings("unchecked")
    public CircularList(int initialCapacity) {
        this.elements = (E[]) new Object[initialCapacity];
        this.head = 0;
        this.size = 0;
        this.maxSize = Integer.MAX_VALUE;
    }

    /**
     * Constructs an empty list with the specified initial capacity
     * and maximum size limit.
     *
     * @param initialCapacity the initial capacity of the list
     * @param maxSize         the maximum number of elements this list can hold
     * @throws IllegalArgumentException if the specified initial capacity is negative,
     *                                  or if maxSize is less than or equal to 0
     */
    @SuppressWarnings("unchecked")
    public CircularList(int initialCapacity, int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        this.elements = (E[]) new Object[initialCapacity];
        this.head = 0;
        this.size = 0;
        this.maxSize = maxSize;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        final Object[] array = new Object[this.size];
        for (int i = 0; i < this.size; i++) {
            array[i] = this.elements[this.realIndex(i)];
        }
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T @NotNull [] toArray(T @NotNull [] array) {
        array = array.length >= this.size ? array
                : (T[]) Array.newInstance(array.getClass().getComponentType(), this.size);

        for (int i = 0; i < this.size; i++) {
            array[i] = (T) this.elements[this.realIndex(i)];
        }

        if (array.length > this.size) {
            array[this.size] = null;
        }

        return array;
    }

    @Override
    public boolean add(E element) {
        if (this.size == this.maxSize) {
            this.elements[this.head] = element;
            this.head = (this.head + 1) % this.elements.length;
        } else {
            this.growIfNeed(this.size + 1);
            this.elements[this.realIndex(this.size)] = element;
            this.size++;
        }
        this.modCount++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        final int index = this.indexOf(o);
        if (index >= 0) {
            this.remove(index);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        if (this.size > 0) {
            Arrays.fill(this.elements, null);
            this.head = 0;
            this.size = 0;
            this.modCount++;
        }
    }

    @Override
    public E get(int index) {
        this.checkIndexForAccess(index);
        return this.elements[this.realIndex(index)];
    }

    @Override
    public E set(int index, E element) {
        this.checkIndexForAccess(index);
        final int realIndex = this.realIndex(index);
        final E old = this.elements[realIndex];
        this.elements[realIndex] = element;
        return old;
    }

    @Override
    public void add(int index, E element) {
        if (index < 0 || index > this.size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
        }

        if (this.size < this.maxSize) {
            this.growIfNeed(this.size + 1);

            if (index < this.size) {
                this.shiftRight(index, this.size - 1);
            }

            this.elements[this.realIndex(index)] = element;
            this.size++;
        } else {
            if (index == 0) {
                this.head = (this.head - 1 + this.elements.length) % this.elements.length;
                this.elements[this.head] = element;
            } else {
                this.shiftRight(index, this.size - 2);
                this.elements[this.realIndex(index)] = element;
            }
        }

        this.modCount++;
    }

    @Override
    public E remove(int index) {
        this.checkIndexForAccess(index);

        final int realIdx = this.realIndex(index);
        final E old = this.elements[realIdx];

        if (index < this.size - 1) {
            this.shiftLeft(index + 1, this.size - 1);
        }

        this.size--;
        this.elements[this.realIndex(this.size)] = null;
        this.modCount++;

        return old;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < this.size; i++) {
                if (this.elements[this.realIndex(i)] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < this.size; i++) {
                if (o.equals(this.elements[this.realIndex(i)])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = this.size - 1; i >= 0; i--) {
                if (this.elements[this.realIndex(i)] == null) {
                    return i;
                }
            }
        } else {
            for (int i = this.size - 1; i >= 0; i--) {
                if (o.equals(this.elements[this.realIndex(i)])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Converts a logical index (0-based from the list perspective) to the
     * actual index in the circular array.
     *
     * @param fake the logical index
     * @return the actual index in the internal array
     */
    private int realIndex(int fake) {
        return (this.head + fake) % this.elements.length;
    }

    /**
     * Validates that the given index is within the valid range for accessing elements.
     *
     * @param i the index to check
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    private void checkIndexForAccess(int i) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + this.size);
        }
    }

    /**
     * Grows the internal array if the specified size exceeds the current capacity.
     * The new capacity is typically double the current capacity, but will not exceed
     * the maximum size limit if one is set.
     *
     * @param newSize the size that needs to be accommodated
     */
    @SuppressWarnings("unchecked")
    private void growIfNeed(int newSize) {
        if (this.elements == EMPTY && newSize <= DEFAULT_CAPACITY) {
            this.elements = (E[]) new Object[DEFAULT_CAPACITY];
            return;
        }

        if (newSize <= this.elements.length) {
            return;
        }

        int newCapacity = Math.max(this.elements.length * 2, newSize);
        newCapacity = Math.min(newCapacity, this.maxSize);

        final E[] newElements = (E[]) new Object[newCapacity];

        for (int i = 0; i < this.size; i++) {
            newElements[i] = this.elements[this.realIndex(i)];
        }

        this.elements = newElements;
        this.head = 0;
    }

    /**
     * Shifts elements to the left in the circular array, effectively removing
     * a gap or moving elements toward the beginning.
     *
     * @param from the starting index (inclusive) for the shift
     * @param to   the ending index (inclusive) for the shift
     */
    private void shiftLeft(int from, int to) {
        for (int i = from; i <= to; i++) {
            final int prevIdx = this.realIndex(i - 1);
            final int currIdx = this.realIndex(i);
            this.elements[prevIdx] = this.elements[currIdx];
        }
    }

    /**
     * Shifts elements to the right in the circular array, creating space
     * for a new element to be inserted.
     *
     * @param from the starting index (inclusive) for the shift
     * @param to   the ending index (inclusive) for the shift
     */
    private void shiftRight(int from, int to) {
        for (int i = to; i >= from; i--) {
            final int currIdx = this.realIndex(i);
            final int nextIdx = this.realIndex(i + 1);
            this.elements[nextIdx] = this.elements[currIdx];
        }
    }
}