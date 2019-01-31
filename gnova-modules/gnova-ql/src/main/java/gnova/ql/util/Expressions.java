package gnova.ql.util;

import gnova.core.annotation.NotNull;
import gnova.core.function.Getter;
import gnova.ql.BooleanExpression;
import gnova.ql.Expression;
import gnova.ql.InvariableExpression;
import gnova.ql.PredicateExpression;
import gnova.ql.ValueExpression;
import gnova.ql.parse.ParseException;
import gnova.ql.parse.ExpressionParser;

import java.util.function.Predicate;

/**
 * 表达式的帮助类
 */
public class Expressions {

    /**
     * 将一个字符串和若干参数转化为一个逻辑表达式
     *
     * @param selection
     * @param params
     * @return
     * @throws IllegalArgumentException
     */
    @NotNull
    public static PredicateExpression toPredicate(String selection, Object[] params)
            throws IllegalArgumentException {

        if (selection == null || (selection = selection.trim()).isEmpty()) {
            // 空的字符串，解析成为一个恒真逻辑表达式
            return InvariableExpression.ALWAYS_TRUE;
        }

        // 1. 解析表达式
        Expression expression;
        try {
            expression = ExpressionParser.parse(selection);
        } catch (ParseException e) {
            // 表达式解析失败
            throw new IllegalArgumentException(e);
        }
        if (expression == null) {
            // 表达式解析失败
            throw new IllegalArgumentException("字符串并非一个逻辑表达式：" + selection);
        } else if (expression.isValue()) {
            // 表达式为值表达式
            ValueExpression value = expression.asValue();
            if (value.isBoolean()) {
                // 布尔值，可以转化为一个恒等的表达式
                return ((BooleanExpression) value).toInvariable();
            } else {
                // 其他表达式
                throw new IllegalArgumentException("字符串并非一个逻辑表达式：" + selection);
            }
        }


        // 2.实例化表达式
        PredicateExpression pe = expression.asPredicate();
        int s = pe.placeholderSize();
        if (s > 0) {
            // 占位符的数量不为0，说明需要实例化
            if (params == null) {
                // 直接返回一个包含了占位符的表达式
                return pe;
            } else if (params.length < s) {
                throw new IllegalArgumentException("占位符的个数（"
                        + s
                        + "）与参数的个数（"
                        + params.length
                        + "）无法匹配");
            }
            pe = ExpressionInstancer.instantiate(pe, params).asPredicate();
        }

        // 3.简化表达式
        return ExpressionSimplifier.simplify(pe).asPredicate();

    }

    @NotNull
    public static Predicate<? extends Getter> toPredicateFunction(String selection, Object[] params)
            throws IllegalArgumentException {
        return toPredicate(selection, params).toFunction();
    }

}
