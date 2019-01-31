package gnova.ql;

import gnova.core.annotation.NotNull;

public interface BooleanExpression extends ValueExpression {

    BooleanExpression TRUE = new BooleanExpressionImpl(true);

    BooleanExpression FALSE = new BooleanExpressionImpl(false);

    /**
     * 转化为恒定的谓词表达式
     *
     * @return
     */
    @NotNull
    InvariableExpression toInvariable();

    @Override
    default ValueType getValueType() {
        return ValueType.Boolean;
    }

    @Override
    Boolean getValue();

    @Override
    default void checkBy(ValuePredicate predicate) throws IllegalArgumentException {
        // 布尔值只能和等于、不等于相匹配
        if (predicate == ValuePredicate.EQ || predicate == ValuePredicate.NEQ) {
            return;
        }
        throw new IllegalArgumentException("布尔值只能和等于、不等于相匹配");
    }
}
