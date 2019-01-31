package gnova.ql;

import gnova.core.annotation.NotNull;
import org.joda.time.DateTime;

import java.util.Date;

public final class TimestampExpressionImpl extends AbstractValueExpression implements TimestampExpression {

    /**
     * 毫秒时间戳
     */
    private final long value;

    public TimestampExpressionImpl() {
        this.value = System.currentTimeMillis();
    }

    public TimestampExpressionImpl(long value) {
        this.value = value;
    }

    @Override
    public Long getValue() {
        return Long.valueOf(value);
    }

    @Override
    public long asInt64(long defaultValue) {
        return value;
    }

    @Override
    public long asTimestamp() {
        return value;
    }

    @Override
    public String toString() {
        return "#" + String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof TimestampExpression)) {
            return false;
        }
        TimestampExpression te = (TimestampExpression) obj;
        return value == te.asTimestamp();
    }

    @Override
    protected int hashing() {
        return Long.hashCode(value);
    }

    @Override
    protected boolean equalsBy(@NotNull Object left) {
        if (left instanceof Number) {
            return ((Number) left).longValue() == value;
        } else if (left instanceof TimestampExpression) {
            return ((TimestampExpression) left).asTimestamp() == value;
        } else if (left instanceof Date) {
            return ((Date) left).getTime() == value;
        } else if (left instanceof DateTime) {
            return ((DateTime) left).getMillis() == value;
        } else if (left instanceof DateExpression) {
            return ((DateExpression) left).asInt64(0L) == value;
        }
        return false;
    }

    @Override
    protected boolean unequalsBy(@NotNull Object left) {
        if (left instanceof Number) {
            return ((Number) left).longValue() != value;
        } else if (left instanceof TimestampExpression) {
            return ((TimestampExpression) left).asTimestamp() != value;
        } else if (left instanceof Date) {
            return ((Date) left).getTime() != value;
        } else if (left instanceof DateTime) {
            return ((DateTime) left).getMillis() != value;
        } else if (left instanceof DateExpression) {
            return ((DateExpression) left).asInt64(0L) != value;
        }
        return false;
    }

    @Override
    protected boolean lessBy(@NotNull Object left) {
        if (left instanceof Number) {
            return ((Number) left).longValue() < value;
        } else if (left instanceof TimestampExpression) {
            return ((TimestampExpression) left).asTimestamp() < value;
        } else if (left instanceof Date) {
            return ((Date) left).getTime() < value;
        } else if (left instanceof DateTime) {
            return ((DateTime) left).getMillis() < value;
        } else if (left instanceof DateExpression) {
            return ((DateExpression) left).asInt64(0L) < value;
        }
        return false;
    }

    @Override
    protected boolean lessEqualsBy(@NotNull Object left) {
        if (left instanceof Number) {
            return ((Number) left).longValue() <= value;
        } else if (left instanceof TimestampExpression) {
            return ((TimestampExpression) left).asTimestamp() <= value;
        } else if (left instanceof Date) {
            return ((Date) left).getTime() <= value;
        } else if (left instanceof DateTime) {
            return ((DateTime) left).getMillis() <= value;
        } else if (left instanceof DateExpression) {
            return ((DateExpression) left).asInt64(0L) <= value;
        }
        return false;
    }

    @Override
    protected boolean greaterBy(@NotNull Object left) {
        if (left instanceof Number) {
            return ((Number) left).longValue() > value;
        } else if (left instanceof TimestampExpression) {
            return ((TimestampExpression) left).asTimestamp() > value;
        } else if (left instanceof Date) {
            return ((Date) left).getTime() > value;
        } else if (left instanceof DateTime) {
            return ((DateTime) left).getMillis() > value;
        } else if (left instanceof DateExpression) {
            return ((DateExpression) left).asInt64(0L) > value;
        }
        return false;
    }

    @Override
    protected boolean greaterEqualsBy(@NotNull Object left) {
        if (left instanceof Number) {
            return ((Number) left).longValue() >= value;
        } else if (left instanceof TimestampExpression) {
            return ((TimestampExpression) left).asTimestamp() >= value;
        } else if (left instanceof Date) {
            return ((Date) left).getTime() >= value;
        } else if (left instanceof DateTime) {
            return ((DateTime) left).getMillis() >= value;
        } else if (left instanceof DateExpression) {
            return ((DateExpression) left).asInt64(0L) >= value;
        }
        return false;
    }
}
