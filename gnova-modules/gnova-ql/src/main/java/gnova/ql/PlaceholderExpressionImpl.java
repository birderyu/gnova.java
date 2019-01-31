package gnova.ql;

public final class PlaceholderExpressionImpl extends AbstractValueExpression implements PlaceholderExpression {

    @Override
    public String toString() {
        return "?";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj instanceof PlaceholderExpression;
    }

    @Override
    protected int hashing() {
        return 54321;
    }
}
