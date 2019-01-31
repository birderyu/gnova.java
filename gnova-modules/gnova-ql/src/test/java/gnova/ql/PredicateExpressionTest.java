package gnova.ql;

import gnova.ql.Expression;
import gnova.ql.parse.ParseException;
import gnova.ql.parse.ExpressionParser;

/**
 * 逻辑表达式测试类
 */
public class PredicateExpressionTest {

    /**
     * 测试简单逻辑表达式
     */
    //@Test
    public void testSimpleExpression() {

        try {
            Expression exp = ExpressionParser.parse("1 = 1");
            boolean b = exp.asPredicate().isAlwaysTrue();

            int stop = 1;
            stop++;
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}
