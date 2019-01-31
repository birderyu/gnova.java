package gnova.ql;

import gnova.core.annotation.Checked;
import gnova.core.annotation.NotNull;

public interface KeyExpression extends ValueExpression {

    @Override
    default ValueType getValueType() {
        return ValueType.Key;
    }

    @Override
    @NotNull
    String getValue();

    @Override
    default void checkBy(ValuePredicate predicate) throws IllegalArgumentException {
        // 键值可以和所有符号相匹配
    }

    @Override
    default String asKey() {
        return getValue();
    }
}
