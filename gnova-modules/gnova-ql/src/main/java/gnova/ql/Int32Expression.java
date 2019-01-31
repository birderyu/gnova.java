package gnova.ql;

public interface Int32Expression extends NumberExpression {

    int intValue();

    @Override
    default ValueType getValueType() {
        return ValueType.Int32;
    }

    @Override
    default Integer getValue() {
        return intValue();
    }

    @Override
    default int asInt32(int defaultValue) {
        return intValue();
    }

    @Override
    default Integer asNumber() {
        return intValue();
    }
}
