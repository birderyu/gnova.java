package gnova.ql;

public interface NumberExpression extends ValueExpression {

    @Override
    Number getValue();

    @Override
    default boolean isNumber() {
        return true;
    }

    @Override
    default void checkBy(ValuePredicate predicate) throws IllegalArgumentException {
        // 数字值只能和等于、不等于、大于、大于等于、小于、小于等于相匹配
        if (predicate == ValuePredicate.EQ || predicate == ValuePredicate.NEQ ||
                predicate == ValuePredicate.GT || predicate == ValuePredicate.GTE ||
                predicate == ValuePredicate.LT || predicate == ValuePredicate.LTE) {
            return;
        }
        throw new IllegalArgumentException("数字值只能和等于、不等于、大于、大于等于、小于、小于等于相匹配");
    }
}
