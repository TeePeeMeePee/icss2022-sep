package nl.han.ica.datastructures;

/**
 * Eigen implementatie van een generieke stack, gebaseerd op een enkelvoudig gelinkte lijst.
 *
 * @param <T> het type van de elementen op de stack
 */
public class HANStack<T> implements IHANStack<T> {

    private static class Node<T> {
        private final T value;
        private final Node<T> next;

        private Node(T value, Node<T> next) {
            this.value = value;
            this.next = next;
        }
    }

    private Node<T> top;

    @Override
    public void push(T value) {
        top = new Node<>(value, top);
    }

    @Override
    public T pop() {
        T value = peek();
        top = top.next;
        return value;
    }

    @Override
    public T peek() {
        if (top == null) {
            throw new java.util.EmptyStackException();
        }
        return top.value;
    }
}
