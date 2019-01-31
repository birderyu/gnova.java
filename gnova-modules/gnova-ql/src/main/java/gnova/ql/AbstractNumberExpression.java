package gnova.ql;

import gnova.core.annotation.NotNull;

public abstract class AbstractNumberExpression extends AbstractValueExpression implements NumberExpression {

    @Override
    protected abstract boolean equalsBy(@NotNull Object left);

    @Override
    protected abstract boolean unequalsBy(@NotNull Object left);

    @Override
    protected abstract boolean lessBy(@NotNull Object left);

    @Override
    protected abstract boolean lessEqualsBy(@NotNull Object left);

    @Override
    protected abstract boolean greaterBy(@NotNull Object left);

    @Override
    protected abstract boolean greaterEqualsBy(@NotNull Object left);

}
