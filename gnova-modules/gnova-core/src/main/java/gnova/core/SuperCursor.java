package gnova.core;

public class SuperCursor<E>
        extends SuperIterator<E> implements Cursor<E> {

    public SuperCursor(Cursor<? extends E> cursor) {
        super(cursor);
    }

    @Override
    public void close() {
        ((Cursor<? extends E>) iterator).close();
    }
}
