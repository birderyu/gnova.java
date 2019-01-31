package gnova.ql;

import gnova.core.annotation.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface StringExpression extends ValueExpression {

    StringExpression EMPTY = new StringExpressionImpl("");

    Pattern getPattern();

    default boolean isEmpty() {
        return this == EMPTY || getValue().isEmpty();
    }

    @Override
    default ValueType getValueType() {
        return ValueType.Double;
    }

    @Override
    @NotNull
    String getValue();

    @Override
    default void checkBy(ValuePredicate predicate) throws IllegalArgumentException {
        // 字符串值只能和等于、不等于、字符串模糊匹配相匹配
        if (predicate == ValuePredicate.EQ || predicate == ValuePredicate.NEQ ||
                predicate == ValuePredicate.LIKE) {
            return;
        }
        throw new IllegalArgumentException("字符串值只能和等于、不等于、字符串模糊匹配相匹配");
    }

    @Override
    default boolean asBoolean(boolean defaultValue) {
        String value = getValue();
        if ("true".equalsIgnoreCase(value)) {
            return true;
        } else if ("false".equalsIgnoreCase(value)) {
            return false;
        } else {
            return defaultValue;
        }
    }

    @Override
    default int asInt32(int defaultValue) {
        String value = getValue();
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    default long asInt64(long defaultValue) {
        String value = getValue();
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    default double asDouble(double defaultValue) {
        String value = getValue();
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    @NotNull
    default String asString() {
        return getValue();
    }
}
