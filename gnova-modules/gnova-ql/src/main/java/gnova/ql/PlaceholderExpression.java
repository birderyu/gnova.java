package gnova.ql;

public interface PlaceholderExpression extends ValueExpression {

    PlaceholderExpression PLACEHOLDER = new PlaceholderExpressionImpl();

    @Override
    default ValueType getValueType() {
        return ValueType.Placeholder;
    }

    @Override
    default Object getValue() {
        return null;
    }

    @Override
    default void checkBy(ValuePredicate predicate) throws IllegalArgumentException {
        // 占位符可以和所有符号相匹配
    }
}
