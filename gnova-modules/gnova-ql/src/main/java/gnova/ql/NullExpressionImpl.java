package gnova.ql;

public final class NullExpressionImpl extends AbstractValueExpression implements NullExpression {

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj instanceof NullExpression;
    }

    @Override
    protected int hashing() {
        return 12345;
    }
}
