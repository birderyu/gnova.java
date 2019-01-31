package gnova.ql;

import java.util.ArrayList;
import java.util.List;

public interface ListExpression extends ValueExpression, Iterable<ValueExpression> {

    /**
     * 空列表
     */
    ListExpression EMPTY = new ListExpressionImpl(new ValueExpression[0]);

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    @Override
    default ValueType getValueType() {
        return ValueType.List;
    }

    @Override
    ValueExpression[] getValue();

    @Override
    default void checkBy(ValuePredicate predicate) throws IllegalArgumentException {
        // 字符串值只能和等于、不等于、列表包含、列表不包含相匹配
        if (predicate == ValuePredicate.EQ || predicate == ValuePredicate.NEQ ||
                predicate == ValuePredicate.IN || predicate == ValuePredicate.NIN) {
            return;
        }
        throw new IllegalArgumentException("字符串值只能和等于、不等于、列表包含、列表不包含相匹配");
    }

    @Override
    default int placeholderSize() {
        int size = 0;
        for (ValueExpression value : this) {
            size += value.placeholderSize();
        }
        return size;
    }

    @Override
    default List asList() {
        List list = new ArrayList();
        for (ValueExpression value : this) {
            if (value.isList()) {
                list.add(value.asList());
            } else {
                list.add(value.getValue());
            }
        }
        return list;
    }
}
