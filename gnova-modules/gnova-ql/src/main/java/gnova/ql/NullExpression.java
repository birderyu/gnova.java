package gnova.ql;

/**
 * 空值表达式
 */
public interface NullExpression extends ValueExpression {

    NullExpression NULL = new NullExpressionImpl();

    @Override
    default ValueType getValueType() {
        return ValueType.Null;
    }

    @Override
    default Object getValue() {
        return null;
    }

    @Override
    default void checkBy(ValuePredicate predicate) throws IllegalArgumentException {
        // 空值只能和等于、不等于相匹配
        // 且除了null = null为true，其余都为false
        if (predicate == ValuePredicate.EQ || predicate == ValuePredicate.NEQ) {
            return;
        }
        throw new IllegalArgumentException("空值只能和等于、不等于相匹配");
    }
}
