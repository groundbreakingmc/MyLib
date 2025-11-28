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
 * limit is reached, attempting to add more elements will throw an
 * {@link IllegalStateException}.</p>
 *
 * <p><strong>Note:</strong> This implementation is not synchronized. If multiple
 * threads access a CircularList instance concurrently, and at least one thread
 * modifies the list structurally, it must be synchronized externally.</p>
 *
 * @param <E> the type of elements in this list
 * @author GroundbreakingMC
 * @version 1.0
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
     * The index in the elements array where the logical first element (index 0) is stored.
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
     * @throws IllegalArgumentException if the specified initial capacity is negative
     *                                  or if maxSize is less than or equal to 0
     */
    @SuppressWarnings("unchecked")
    public CircularList(int initialCapacity, int maxSize) {
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
        if (o == null) {
            for (int i = 0; i < this.size; i++) {
                if (this.elements[i] == null) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < this.size; i++) {
                if (this.elements[i].equals(o)) {
                    return true;
                }
            }
        }
        return false;
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
        this.growIfNeed(this.size + 1);
        int realIndex = this.realIndex(this.size);
        this.elements[realIndex] = element;
        this.size++;
        this.modCount++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            for (int i = 0; i < this.size; i++) {
                if (this.elements[i] == null) {
                    this.remove(i);
                    return true;
                }
            }
        } else {
            for (int i = 0; i < this.size; i++) {
                if (this.elements[i].equals(o)) {
                    this.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void clear() {
        Arrays.fill(this.elements, null);
        this.head = 0;
        this.size = 0;
        this.modCount++;
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

        growIfNeed(this.size + 1);

        if (index == this.size) {
            this.elements[realIndex(this.size)] = element;
        } else {
            // Сдвигаем элементы вправо
            shiftRight(index, this.size - 1);
            this.elements[realIndex(index)] = element;
        }

        this.size++;
        this.modCount++;
    }

    @Override
    public E remove(int index) {
        this.checkIndexForAccess(index);

        final int realIndex = this.realIndex(index);
        final E old = this.elements[realIndex];

        shiftLeft(index + 1, this.size - 1);

        this.size--;
        this.elements[realIndex(this.size)] = null;
        this.modCount++;

        return old;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < this.size; i++) {
                final int realIndex = this.realIndex(i);
                if (this.elements[realIndex] == null) {
                    return realIndex;
                }
            }
        } else {
            for (int i = 0; i < this.size; i++) {
                final int realIndex = this.realIndex(i);
                if (this.elements[realIndex].equals(o)) {
                    return realIndex;
                }
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = this.size - 1; i >= 0; i--) {
                final int realIndex = this.realIndex(i);
                if (this.elements[realIndex] == null) {
                    return realIndex;
                }
            }
        } else {
            for (int i = this.size - 1; i >= 0; i--) {
                final int realIndex = this.realIndex(i);
                if (this.elements[realIndex].equals(o)) {
                    return realIndex;
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
        final int sum = this.head + fake;
        return sum > this.elements.length ? sum - this.elements.length : sum;
    }

    /**
     * Validates that the given index is within the valid range for accessing elements.
     *
     * @param i the index to check
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    private void checkIndexForAccess(int i) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException("Index (" + i + ") is greater than or equal to list size (" + this.size + ")");
        }
    }

    /**
     * Grows the internal array if the specified size would exceed the current capacity.
     * The new capacity is typically double the current capacity, but will not exceed
     * the maximum size limit if one is set.
     *
     * @param newSize the size that needs to be accommodated
     * @throws IllegalStateException if the new size would exceed the maximum size limit
     */
    @SuppressWarnings("unchecked")
    private void growIfNeed(int newSize) {
        if (this.elements == EMPTY && newSize < DEFAULT_CAPACITY) {
            this.elements = (E[]) new Object[DEFAULT_CAPACITY];
            return;
        }
        if (newSize <= this.elements.length) {
            return;
        }

        if (newSize > this.maxSize) {
            throw new IllegalStateException("Cannot grow beyond maxSize: " + this.maxSize);
        }

        int newCapacity = Math.max(this.elements.length * 2, newSize);
        newCapacity = Math.min(newCapacity, this.maxSize);

        final E[] newElements = (E[]) new Object[newCapacity];

        for (int i = 0; i < this.size; i++) {
            newElements[i] = this.elements[realIndex(i)];
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
            int prevRealIndex = realIndex(i - 1);
            int currRealIndex = realIndex(i);
            this.elements[prevRealIndex] = this.elements[currRealIndex];
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
            int currRealIndex = realIndex(i);
            int nextRealIndex = realIndex(i + 1);
            this.elements[nextRealIndex] = this.elements[currRealIndex];
        }
    }
}
