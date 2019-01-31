package gnova.ql;

import gnova.core.annotation.NotNull;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * 时间戳
 */
public interface TimestampExpression extends ValueExpression {

    @Override
    default ValueType getValueType() {
        return ValueType.Timestamp;
    }

    @Override
    Long getValue();

    @Override
    default long asInt64(long defaultValue) {
        return getValue().longValue();
    }

    @Override
    default long asTimestamp() {
        return getValue().longValue();
    }

    @Override
    default void checkBy(ValuePredicate predicate) throws IllegalArgumentException {
        // 时间戳值只能和等于、不等于、大于、大于等于、小于、小于等于相匹配
        if (predicate == ValuePredicate.EQ || predicate == ValuePredicate.NEQ ||
                predicate == ValuePredicate.GT || predicate == ValuePredicate.GTE ||
                predicate == ValuePredicate.LT || predicate == ValuePredicate.LTE) {
            return;
        }
        throw new IllegalArgumentException("时间戳只能和等于、不等于、大于、大于等于、小于、小于等于相匹配");
    }
}
