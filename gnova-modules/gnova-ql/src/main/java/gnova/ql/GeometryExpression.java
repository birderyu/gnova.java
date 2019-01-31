package gnova.ql;

import gnova.core.annotation.NotNull;
import gnova.geometry.model.*;

public interface GeometryExpression extends ValueExpression {

    GeometryExpression EMPTY = new GeometryExpressionImpl(Geometry.NONE);

    default boolean isEmpty() {
        return this == EMPTY || getValue().isEmpty();
    }

    @Override
    default ValueType getValueType() {
        return ValueType.Geometry;
    }

    @Override
    @NotNull
    Geometry getValue();

    @Override
    default void checkBy(ValuePredicate predicate) throws IllegalArgumentException {
        // 几何区域值只能和等于、不等于、几何相交、几何包含相匹配
        if (predicate == ValuePredicate.EQ || predicate == ValuePredicate.NEQ ||
                predicate == ValuePredicate.INTERSECT || predicate == ValuePredicate.WITHIN) {
            return;
        }
        throw new IllegalArgumentException("几何区域值只能和等于、不等于、几何相交、几何包含相匹配");
    }

    @Override
    default Geometry asGeometry() {
        return getValue();
    }


}
