package net.glintwein.util;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class UnmodifiableArrayList<T> extends AbstractList<T> {
    private static final UnmodifiableArrayList<Object> EMPTY = new UnmodifiableArrayList<Object>(new Object[0]) {
        @Override
        public Iterator<Object> iterator() {
            return Collections.emptyIterator();
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> UnmodifiableArrayList<T> empty() {
        return (UnmodifiableArrayList<T>) EMPTY;
    }

    private final T[] array;

    public UnmodifiableArrayList(T[] array) {
        this.array = array;
    }

    public UnmodifiableArrayList<T> cloneAdd(T element) {
        T[] newArray = Arrays.copyOf(array, array.length + 1);
        newArray[array.length] = element;
        return new UnmodifiableArrayList<>(newArray);
    }

    public UnmodifiableArrayList<T> cloneAdd(int index, T element) {
        if (index < 0 || index > array.length)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + array.length);

        T[] newArray = Arrays.copyOf(array, array.length + 1);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return new UnmodifiableArrayList<>(newArray);
    }

    public UnmodifiableArrayList<T> cloneRemove(int index) {
        if (index < 0 || index >= array.length)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + array.length);

        if (array.length == 1)
            return empty();
        T[] newArray = Arrays.copyOf(array, array.length - 1);
        if (index < array.length - 1)
            System.arraycopy(array, index + 1, newArray, index, array.length - index - 1);
        return new UnmodifiableArrayList<>(newArray);
    }

    @Override
    public T get(int index) {
        return array[index];
    }

    @Override
    public int indexOf(Object o) {
        T[] a = this.array;
        if (o == null) {
            for (int i = 0; i < a.length; i++)
                if (a[i] == null)
                    return i;
        } else {
            for (int i = 0; i < a.length; i++)
                if (o.equals(a[i]))
                    return i;
        }
        return -1;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        for (T e : array) {
            action.accept(e);
        }
    }

    @Override
    public void sort(@Nullable Comparator<? super T> c) {
        throw new UnsupportedOperationException("UnmodifiableArrayList cannot be sorted");
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(array, Spliterator.ORDERED);
    }

    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    @Override
    public Object[] toArray() {
        return array.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E[] toArray(E[] a) {
        int size = size();
        if (a.length < size)
            return Arrays.copyOf(this.array, size,
                (Class<? extends E[]>) a.getClass());
        System.arraycopy(this.array, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    private class Itr implements Iterator<T> {
        int cursor;

        Itr() {
        }

        @Override
        public boolean hasNext() {
            return cursor != array.length;
        }

        @Override
        public T next() {
            int i = cursor;
            T[] elementData = array;
            if (i >= elementData.length)
                throw new NoSuchElementException();
            cursor = i + 1;
            return elementData[i];
        }

        @Override
        public void forEachRemaining(Consumer<? super T> consumer) {
            Objects.requireNonNull(consumer);
            final int size = array.length;
            int i = cursor;
            if (i >= size) {
                return;
            }
            while (i != size) {
                consumer.accept(array[i++]);
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i;
        }
    }
}
