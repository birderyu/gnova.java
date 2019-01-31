package gnova.ql;

public interface DoubleExpression extends NumberExpression {

    double doubleValue();

    @Override
    default ValueType getValueType() {
        return ValueType.Double;
    }

    @Override
    default Double getValue() {
        return doubleValue();
    }

    @Override
    default double asDouble ( double defaultValue){
        return doubleValue();
    }

    @Override
    default Double asNumber() {
        return doubleValue();
    }
}