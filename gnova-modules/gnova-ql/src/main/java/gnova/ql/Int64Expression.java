package gnova.ql;

public interface Int64Expression extends NumberExpression {

    long longValue();

    @Override
    default ValueType getValueType() {
        return ValueType.Int64;
    }

    @Override
    default Long getValue() {
        return longValue();
    }

    @Override
    default long asInt64(long defaultValue) {
        return longValue();
    }

    @Override
    default Long asNumber() {
        return longValue();
    }

}
