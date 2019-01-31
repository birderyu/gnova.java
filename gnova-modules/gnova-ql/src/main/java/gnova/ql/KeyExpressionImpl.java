package gnova.ql;

import gnova.core.annotation.Checked;
import gnova.core.annotation.NotNull;

public final class KeyExpressionImpl extends AbstractValueExpression implements KeyExpression {

    @NotNull
    private final String name;

    public KeyExpressionImpl(@Checked String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public String getValue() {
        return name;
    }

    @Override
    public String asKey() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof KeyExpression)) {
            return false;
        }
        KeyExpression key = (KeyExpression) obj;
        return name.equals(key.asKey());
    }

    @Override
    protected int hashing() {
        return name.hashCode();
    }
}
