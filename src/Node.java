import java.util.Iterator;

public class Node<T> implements Iterable<T> {
    final T last;
    final Node<T> prev;
    final int size;

    public Node(T last, Node<T> prev) {
        this.last = last;
        this.prev = prev;
        size = prev.size + 1;
    }

    public Node() {
        last = null;
        prev = null;
        size = 0;
    }

    public Iterator<T> iterator() {
        Node<T> start = this;
        return new Iterator<T>() {
            Node<T> current = start;

            @Override
            public boolean hasNext() {
                return current.size != 0;
            }

            @Override
            public T next() {
                T result = current.last;
                current = current.prev;
                return result;
            }
        };
    }

    public String toString() {
        StringBuilder result = new StringBuilder("[");
        Node i = this;
        while (i.size != 0) {
            result.append(i.last);
            i = i.prev;
            if (i.size != 0) {
                result.append(", ");
            }
        }

        return result + "]";
    }
}