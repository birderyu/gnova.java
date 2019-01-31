package gnova.ql.parse;

import gnova.core.ArrayUtil;
import gnova.core.annotation.Checked;
import gnova.core.annotation.NotNull;
import gnova.geometry.model.FactoryFinder;
import gnova.geometry.model.GeometryFactory;
import gnova.geometry.model.*;
import gnova.ql.BooleanExpression;
import gnova.ql.BytesExpression;
import gnova.ql.BytesExpressionImpl;
import gnova.ql.DateExpression;
import gnova.ql.DoubleExpression;
import gnova.ql.DoubleExpressionImpl;
import gnova.ql.Expression;
import gnova.ql.GeometryExpression;
import gnova.ql.GeometryExpressionImpl;
import gnova.ql.Int32Expression;
import gnova.ql.Int32ExpressionImpl;
import gnova.ql.Int64Expression;
import gnova.ql.Int64ExpressionImpl;
import gnova.ql.KeyExpression;
import gnova.ql.KeyExpressionImpl;
import gnova.ql.ListExpression;
import gnova.ql.ListExpressionImpl;
import gnova.ql.LogicalPredicate;
import gnova.ql.NullExpression;
import gnova.ql.PlaceholderExpression;
import gnova.ql.StringExpression;
import gnova.ql.StringExpressionImpl;
import gnova.ql.ValueExpression;
import gnova.ql.ValuePredicate;
import gnova.ql.util.ExpressionFactory;

import java.util.*;

public class ExpressionParser {

    @NotNull
    public static Expression parse(@Checked String whereClause)
            throws ParseException {

        if (whereClause == null || whereClause.isEmpty()) {
            throw new ParseException("字符串为空", whereClause, -1);
        }
        StringStream stream = preParse(whereClause,
                0, whereClause.length());
        if (stream.isEmpty()) {
            throw new ParseException("字符串中没有有效的信息", whereClause, -1);
        }

        return parse(stream);

    }

    @NotNull
    private static Expression parse(StringStream stream)
            throws ParseException {

        ParserStack stack = new ParserStack(stream.getValue());

        // 处理列表值
        Collection<ValueExpression> values = null;

        int valueStartCursor = stream.getBeginIndex(); // 值表达式开始的位置
        boolean valueStarted = true;
        while (stream.hasCurrent()) {

            //
            BracketScope bracketScope = stream.inBracket();
            if (bracketScope != null) {
                // 位于括号之内
                if (bracketScope.isParenthesis()) {
                    // 小括号
                    Expression exp = parseParenthesis(stream.subString(
                            bracketScope.getBeginIndex() + 1,
                            bracketScope.getEndIndex() - 1));
                    if (exp == null) {
                        throw new ParseException("解析失败，括号内部解析失败",
                                stream.getValue(), stream.getCursor());
                    } else if (exp.isValue()) {
                        // 解析出一个值表达式
                        if (values != null && !values.isEmpty()) {
                            values.add(exp.asValue());
                        } else {
                            stack.push(exp, stream.getCursor());
                        }
                    } else {
                        // 解析出一个逻辑表达式
                        stack.push(exp, stream.getCursor());
                    }
                } else if (bracketScope.isSquare()) {
                    // 方括号，解析成一个字节串值
                    if (bracketScope.getParent() != null &&
                            bracketScope.getParent().isSquare()) {
                        // 该方括号有一个父括号，且该父括号的也为方括号，则解析失败
                        // 因为第一次出现的方括号是一个标识符，它是不允许嵌套的
                        throw new ParseException("解析失败，前一个中括号未解析成功。",
                                stream.getValue(), stream.getCursor());
                    }
                    BytesExpression value = parseBytes(stream.subString(
                            bracketScope.getBeginIndex() + 1,
                            bracketScope.getEndIndex() - 1));
                    if (values != null && !values.isEmpty()) {
                        values.add(value);
                    } else {
                        stack.push(value, stream.getCursor());
                    }
                } else {
                    // 花括号，解析成一个几何区域值
                    if (bracketScope.getParent() != null &&
                            bracketScope.getParent().isCurly()) {
                        // 该花括号有一个父括号，且该父括号的也为花括号，则解析失败
                        // 因为第一次出现的花括号是一个标识符，它是不允许嵌套的
                        throw new ParseException("解析失败，前一个花括号未解析成功。",
                                stream.getValue(), stream.getCursor());
                    }
                    GeometryExpression value = parseGeometry(stream.subString(
                            bracketScope.getBeginIndex() + 1,
                            bracketScope.getEndIndex() - 1));
                    if (values != null && !values.isEmpty()) {
                        values.add(value);
                    } else {
                        stack.push(value, stream.getCursor());
                    }
                }
                // 将光标移动到括号之外
                stream.nextTo(bracketScope.getEndIndex());

                // 重新开始解析一个值
                valueStarted = false;
                continue;
            }

            QuotationScope quotationScope = stream.inQuotation();
            if (quotationScope != null) {
                // 位于引号内，解析成字符串
                StringExpression value = parseString(stream.subString(
                        quotationScope.getBeginIndex() + 1,
                        quotationScope.getEndIndex() - 1));
                if (values != null && !values.isEmpty()) {
                    values.add(value);
                } else {
                    stack.push(value, stream.getCursor());
                }
                // 将光标移动到引号之外
                stream.nextTo(quotationScope.getEndIndex());

                // 重新开始解析一个值
                valueStarted = false;
                continue;
            }

            // 不在括号内，也不在引号内
            // 验证是否是逻辑操作符
            Iterator<LogicalPredicate> loCatchOrder = LogicalPredicate.getMatchOrder();
            boolean loMatched = false;
            while (loCatchOrder.hasNext()) {
                LogicalPredicate lo = loCatchOrder.next();
                if (stream.matches(lo.getSymbol(), lo.getPrefix(), lo.getSuffix())) {
                    // 匹配成功当前的逻辑操作符
                    loMatched = true;
                    stack.push(lo, stream.getCursor());

                    // 将光标移动到逻辑操作符之外
                    stream.nextN(lo.getSymbol().length());

                    // 重新开始解析一个值
                    valueStarted = false;
                    break;
                }
            }
            if (loMatched) {
                // 重新开始解析一个值
                valueStarted = false;
                continue;
            }

            // 验证是否是比较操作符
            Iterator<ValuePredicate> coCatchOrder = ValuePredicate.getMatchOrder();
            boolean coMatched = false;
            while (coCatchOrder.hasNext()) {
                ValuePredicate co = coCatchOrder.next();
                if (stream.matches(co.getSymbol(), co.getPrefix(), co.getSuffix())) {
                    // 匹配成功当前的逻辑操作符
                    coMatched = true;
                    stack.push(co, stream.getCursor());

                    // 将光标移动到逻辑操作符之外
                    stream.nextN(co.getSymbol().length());

                    // 重新开始解析一个值
                    valueStarted = false;
                    break;
                }
            }
            if (coMatched) {
                // 重新开始解析一个值
                valueStarted = false;
                continue;
            }

            // 验证是否是一些比较特殊的值
            char c = stream.getCurrent();
            if (c == '@') {
                // 可能是一个日期值
                if (stream.getNextN(1) == '(') {
                    // 日期值之后应该紧跟圆括号，否则不会被解析成一个日期值
                    stream.next();
                    BracketScope dateBracketScope = stream.inBracket();
                    if (dateBracketScope == null || !dateBracketScope.isParenthesis()) {
                        throw new ParseException("日期值必须使用圆括号括起来",
                                stream.getValue(), stream.getCursor());
                    }
                    DateExpression de = parseDate(stream.subString(
                            dateBracketScope.getBeginIndex() + 1,
                            dateBracketScope.getEndIndex() - 1));

                    if (values != null && !values.isEmpty()) {
                        values.add(de);
                    } else {
                        stack.push(de, stream.getCursor());
                    }
                    // 将光标移动到括号之外
                    stream.nextTo(dateBracketScope.getEndIndex());

                    // 重新开始解析一个值
                    valueStarted = false;
                    continue;

                }
            } else if (c == '!') {
                // 否定表达式
                stack.pushNegative();

                // 重新开始解析一个值
                valueStarted = false;
            } else if (c == ' ') {
                // 空格，开始解析一个值
                if (valueStarted) {
                    ValueExpression ve = parseValue(stream.subString(
                            valueStartCursor, stream.getCursor()));
                    if (values != null && !values.isEmpty()) {
                        values.add(ve);
                    } else {
                        stack.push(ve, stream.getCursor());
                    }

                    // 重新开始解析一个值
                    valueStarted = false;
                }
            } else if (c == ',') {
                // 逗号，开始解析一个列表值
                if (valueStarted) {
                    Expression ve = parse(stream.subString(
                            valueStartCursor, stream.getCursor()));
                    if (!ve.isValue()) {
                        throw new ParseException("逗号前必须是一个值表达式",
                                stream.getValue(), stream.getCursor());
                    }
                    if (values == null) {
                        values = new ArrayList<>();
                    }
                    values.add(ve.asValue());

                    // 重新开始解析一个值
                    valueStarted = false;
                } else {
                    // 说明前一个值应该是列表值的一部分
                    ValueExpression ve = stack.pop();
                    if (ve != null) {
                        if (values == null) {
                            values = new ArrayList<>();
                        }
                        values.add(ve);
                    }
                }
            } else {
                if (!valueStarted) {
                    valueStarted = true;
                    valueStartCursor = stream.getCursor();
                }
            }

            stream.next();
        }

        // 还有未解析的值
        ValueExpression ve = null;
        if (valueStarted) {
            ve = parseValue(stream.subString(
                    valueStartCursor, stream.getEndIndex()));
            if (values != null && !values.isEmpty()) {
                values.add(ve);
                ve = new ListExpressionImpl(values);
            }
        } else {
            if (values != null && !values.isEmpty()) {
                ve = new ListExpressionImpl(values);
            }
        }

        if (ve != null) {
            stack.push(ve, stream.getCursor());
        }

        return stack.get();

    }

    /**
     * 解析小括号
     * @param stream
     * @return
     */
    private static Expression parseParenthesis(StringStream stream)
            throws ParseException {
        stream = stream.trim();
        if (stream.isEmpty()) {
            // 空括号
            return ListExpression.EMPTY;
        }
        Expression exp = parse(stream);
        if (exp.isValue() && !exp.asValue().isList()) {
            // 处理列表中只包含一个值的情况
            return new ListExpressionImpl(new ValueExpression[] {exp.asValue()});
        }
        return exp;
    }

    private static ValueExpression parseValue(StringStream stream)
            throws ParseException {

        // 注意，字符串、列表、几何区域已经在其他地方处理了
        // 这里的结果只可能是占位符、空值、布尔值、数字值和键值

        //stream = stream.trim();
        if (stream.isEmpty()) {
            throw new ParseException("值表达式解析失败，值不允许为空",
                    stream.getValue(), stream.getCursor());
        }

        char current = stream.getCurrent();
        if (current == '?' && stream.size() == 1) {
            // 占位符
            return PlaceholderExpression.PLACEHOLDER;
        } else if (stream.matches("null", null, null) &&
                stream.size() == 4) {
            // 空值
            return NullExpression.NULL;
        } else if (stream.matches("true", null, null) &&
                stream.size() == 4) {
            // 布尔真值
            return BooleanExpression.TRUE;
        } else if (stream.matches("false", null, null) &&
                stream.size() == 5) {
            // 布尔假值
            return BooleanExpression.FALSE;
        } else if (current == '#') {
            // 时间戳值
            try {
                long value = Long.valueOf(stream.subString(1, stream.getEndIndex()).toString());
                return ExpressionFactory.buildTimestamp(value);
            } catch (NumberFormatException nfe) {
                throw new ParseException("时间戳值必须是一个井号和一个数字字面值的组合",
                        stream.getValue(), stream.getCursor());
            }
        } else {
            String v = stream.toString();
            char c = v.charAt(0);
            if (charIsNumber(c) || c == '-') {
                // 可能是数字，试着转换为数字
                try {
                    long lv = Long.valueOf(v);
                    if (lv <= MAX_INT_32 && lv >= MIN_INT_32) {
                        return new Int32ExpressionImpl((int) lv);
                    } else {
                        return new Int64ExpressionImpl(lv);
                    }
                } catch (NumberFormatException e) {
                    try {
                        double dv = Double.valueOf(v);
                        return new DoubleExpressionImpl(dv);
                    } catch (NumberFormatException ex) {
                        // do nothing
                    }
                }
                throw new ParseException("键值只能够以字母和下划线作为开头",
                        stream.getValue(), stream.getCursor());
            } else {
                // 键值
                if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
                    return new KeyExpressionImpl(v);
                } else {
                    throw new ParseException("键值只能够以字母和下划线作为开头",
                            stream.getValue(), stream.getCursor());
                }

            }
        }

    }

    private static DateExpression parseDate(StringStream stream) throws ParseException {
        if (stream.isEmpty()) {
            throw new ParseException("日期值表达式解析失败，值不允许为空",
                    stream.getValue(), stream.getCursor());
        }

        List<StringStream> dateFormat = new ArrayList<>();
        boolean lastStopped = false;
        while (stream.hasCurrent()) {
            QuotationScope quotationScope = stream.inQuotation();
            if (quotationScope != null) {
                // 位于引号内
                dateFormat.add(stream.subString(
                        quotationScope.getBeginIndex() + 1,
                        quotationScope.getEndIndex() - 1));
                // 将光标移动到引号之外
                stream.nextTo(quotationScope.getEndIndex());
                lastStopped = true;
                continue;
            }

            char c = stream.getCurrent();
            // 引号之间只允许出现逗号和空格
            if (c == ',') {
                lastStopped = false;
            } else if (c == ' ') {
                // do nothing
            } else {
                throw new ParseException("日期值表达式解析失败，格式不正确：日期之间不允许有除了逗号和空格之外的其他符号",
                        stream.getValue(), stream.getCursor());
            }
            stream.next();
        }

        if (!lastStopped) {
            throw new ParseException("日期值表达式解析失败，格式不正确：逗号之后不包含任何值",
                    stream.getValue(), stream.getCursor());
        }

        if (dateFormat.size() == 1) {
            try {
                return ExpressionFactory.buildDate(dateFormat.get(0).toString());
            } catch (UnsupportedOperationException uoe) {
                throw new ParseException("日期值表达式解析失败",
                        stream.getValue(), stream.getCursor(), uoe);
            } catch (IllegalArgumentException iae) {
                throw new ParseException("日期值表达式解析失败",
                        stream.getValue(), stream.getCursor(), iae);
            }
        } else if (dateFormat.size() == 2) {
            try {
                return ExpressionFactory.buildDate(dateFormat.get(0).toString(),
                        dateFormat.get(1).toString());
            } catch (UnsupportedOperationException uoe) {
                throw new ParseException("日期值表达式解析失败",
                        stream.getValue(), stream.getCursor(), uoe);
            } catch (IllegalArgumentException iae) {
                throw new ParseException("日期值表达式解析失败",
                        stream.getValue(), stream.getCursor(), iae);
            }
        }
        throw new ParseException("日期值表达式解析失败，格式不正确",
                stream.getValue(), stream.getCursor());
    }

    /**
     * 解析字节串对象
     *
     * @param stream 字符串流，这个流已经截掉了作为字节串标识符的中括号
     * @return
     */
    @NotNull
    private static BytesExpression parseBytes(StringStream stream)
            throws ParseException {

        if (stream.isEmpty()) {
            return BytesExpression.EMPTY;
        }

        // FF FF FF FF FF FF FF FF FF
        List<Byte> bytes = new ArrayList<>();
        int startAfterEmptyIndex = -1;
        while (stream.hasCurrent()) {
            if (stream.getCurrent() != ' ') {
                // 解析到了一个非空格，判断是否是空格之后的第一个非空格
                if (startAfterEmptyIndex == -1) {
                    // 第一个非空格，记录下游标
                    startAfterEmptyIndex = stream.getCursor();
                }
            } else {
                // 解析到了一个空格，判断是否是非空格之后的第一个空格
                if (startAfterEmptyIndex != -1) {
                    // 第一个空格，解析一个字节
                    bytes.add(parseByte(stream.subString(startAfterEmptyIndex, stream.getCursor())));
                    startAfterEmptyIndex = -1;
                }
            }
            stream.next();
        }

        // 解析最后一个字节
        if (startAfterEmptyIndex != -1) {
            bytes.add(parseByte(stream.subString(startAfterEmptyIndex, stream.getCursor())));
        }
        if (bytes.isEmpty()) {
            return BytesExpression.EMPTY;
        }
        return new BytesExpressionImpl(ArrayUtil.unboxing(bytes.toArray(new Byte[bytes.size()])));
    }

    private static Byte parseByte(StringStream stream) throws ParseException {

        if (stream.isEmpty()) {
            throw new ParseException("字节类型解析失败，不允许为空",
                    stream.getValue(), stream.getCursor());
        }

        // 解析一个十六进制的数字
        int r = 0;
        while (stream.hasCurrent()) {
            char c = stream.getCurrent();
            int _r = 0;
            if (c == '0') {
                _r = 0;
            } else if (c == '1') {
                _r = 1;
            } else if (c == '2') {
                _r = 2;
            } else if (c == '3') {
                _r = 3;
            } else if (c == '4') {
                _r = 4;
            } else if (c == '5') {
                _r = 5;
            } else if (c == '6') {
                _r = 6;
            } else if (c == '7') {
                _r = 7;
            } else if (c == '8') {
                _r = 8;
            } else if (c == '9') {
                _r = 9;
            } else if (c == 'a' || c == 'A') {
                _r = 10;
            } else if (c == 'b' || c == 'B') {
                _r = 11;
            } else if (c == 'c' || c == 'C') {
                _r = 12;
            } else if (c == 'd' || c == 'D') {
                _r = 13;
            } else if (c == 'e' || c == 'E') {
                _r = 14;
            } else if (c == 'f' || c == 'F') {
                _r = 15;
            } else {
                throw new ParseException("字节类型解析失败，无法解析的十六进制数字值：" + c,
                        stream.getValue(), stream.getCursor());
            }
            r = (r * 16) + _r;
            stream.next();
        }
        // 一个字节的取值范围为[0, 255]
        if (r > 255) {
            throw new ParseException("字节类型解析失败，单个数字超过字节的最大值255",
                    stream.getValue(), stream.getCursor());
        }
        // 注意，java中byte是有符号的，当值大于127时，会显示成补码的形式，如128为-128、129为-127、...、255为-1
        // 但精度不会降低，如需要将有符号的byte转换为无符号的byte，只需在原值上加上256即可
        return Byte.valueOf((byte) r);
    }

    /**
     * 解析几何对象
     *
     * @param stream 字符串流，这个流已经截掉了作为几何对象标识符的花括号
     * @return
     */
    @NotNull
    private static GeometryExpression parseGeometry(StringStream stream)
            throws ParseException {

        if (stream.isEmpty()) {
            return GeometryExpression.EMPTY;
        }

        List<Polygon> polygons = null;
        while (stream.hasCurrent()) {
            BracketScope bracketScope = stream.inBracket();
            if (bracketScope != null) {
                if (bracketScope.isSquare()) {
                    // 解析成Polygon
                    Polygon polygon = parsePolygon(stream.subString(
                            bracketScope.getBeginIndex() + 1,
                            bracketScope.getEndIndex() - 1));
                    if (polygons == null) {
                        polygons = new ArrayList<>();
                    }
                    polygons.add(polygon);
                } else if (bracketScope.isParenthesis()) {
                    throw new ParseException("几何区域值解析失败，几何区域值必须由一个或多个多边形组成，不允许由线环值组成",
                            stream.getValue(), stream.getCursor());
                } else {
                    throw new ParseException("几何区域值解析失败，花括号内部不允许嵌套花括号",
                            stream.getValue(), stream.getCursor());
                }
                stream.nextTo(bracketScope.getEndIndex());
                continue;
            }
            stream.next();
        }

        if (polygons == null || polygons.isEmpty()) {
            return GeometryExpression.EMPTY;
        } else {
            if (polygons.size() == 1) {
                return new GeometryExpressionImpl(polygons.get(0));
            } else {
                MultiPolygon multiPolygon;
                try {
                    multiPolygon = GEOMETRY_FACTORY.createMultiPolygon(polygons);
                } catch (Exception e) {
                    throw new ParseException("坐标解析失败，坐标无法构成多多边形",
                            stream.getValue(), stream.getCursor());
                }
                return new GeometryExpressionImpl(multiPolygon);
            }
        }
    }

    @NotNull
    private static StringExpression parseString(StringStream stream) {

        if (stream.isEmpty()) {
            return StringExpression.EMPTY;
        }
        return new StringExpressionImpl(stream.toString());
    }

    /**
     * 预解析
     *
     * <p>预解析的作用是将字符串中的括号与引号解析出来，并将字符串转换为{@link StringStream} 对象</p>
     *
     * @param whereClause
     * @param beginIndex
     * @param endIndex
     * @return
     */
    @NotNull
    private static StringStream preParse(@NotNull String whereClause,
                                         @Checked int beginIndex,
                                         @Checked int endIndex)
            throws ParseException {

        StringStream s = new StringStream(whereClause,
                beginIndex, endIndex, null, null);
        if (s.isEmpty()) {
            return s;
        }

        Deque<QuotationScope> quotationScopes = null; // 引号的范围
        Deque<BracketScope> bracketScopes = null; // 括号的范围
        boolean inQuotation = false; // 是否位于引号中
        int leftDoubleQuotationCursor = -1; // 上一个左双引号的下标
        int leftSingleQuotationCursor = -1; // 上一个左单引号的下标
        Stack<BracketScope> bracketScopeStack = null; // 括号栈

        while (s.hasCurrent()) {
            char c = s.getCurrent();
            if (inQuotation) {
                // 位于引号中
                if (leftSingleQuotationCursor != -1 &&
                        c == '\'') {
                    if (s.hasPrevious() &&
                            s.getPreviousN(1) == '\\') {
                        // 转义符，直接忽略
                        // do nothing
                    } else {
                        // 非转义符，引号结束
                        // 将引号的首尾下标存入数组
                        if (quotationScopes == null) {
                            quotationScopes = new LinkedList<>();
                        }
                        QuotationScope quotationScope = new QuotationScope(
                                leftSingleQuotationCursor,
                                s.getCursor() + 1);
                        quotationScopes.add(quotationScope);
                        inQuotation = false;
                        leftSingleQuotationCursor = -1;
                    }
                } else if (leftDoubleQuotationCursor != -1 &&
                        c == '"') {
                    if (s.hasPrevious() &&
                            s.getPreviousN(1) == '\\') {
                        // 转义符，直接忽略
                        // do nothing
                    } else {
                        // 非转义符，引号结束
                        // 将引号的首尾下标存入数组
                        if (quotationScopes == null) {
                            quotationScopes = new LinkedList<>();
                        }
                        QuotationScope quotationScope = new QuotationScope(
                                leftDoubleQuotationCursor,
                                s.getCursor() + 1);
                        quotationScopes.add(quotationScope);
                        inQuotation = false;
                        leftDoubleQuotationCursor = -1;
                    }
                }
            } else {
                // 不位于引号中
                if (c == '\'') {
                    if (s.hasPrevious() &&
                            s.getPreviousN(1) == '\\') {
                        // 转义符，直接忽略
                        // do nothing
                    } else if (leftDoubleQuotationCursor != -1 ) {
                        // 虽然遇到了一个单引号，但是在双引号之中，如："I'm good."
                        // do nothing
                    } else {
                        leftSingleQuotationCursor = s.getCursor();
                        inQuotation = true;
                    }
                } else if (c == '"') {
                    if (s.hasPrevious() &&
                            s.getPreviousN(1) == '\\') {
                        // 转义符，直接忽略
                        // do nothing
                    } else if (leftSingleQuotationCursor != -1 ) {
                        // 虽然遇到了一个双引号，但是在单引号之中，如：'He is a "GOOD" man.'"
                        // do nothing
                    } else {
                        leftDoubleQuotationCursor = s.getCursor();
                        inQuotation = true;
                    }
                } else if (c == '(') {
                    // 左圆括号
                    if (bracketScopeStack == null) {
                        bracketScopeStack = new Stack<>();
                    }
                    if (bracketScopeStack.isEmpty()) {
                        // 无嵌套的括号
                        BracketScope bracketScope = new BracketScope(null,
                                BracketScope.Type.Parenthesis,
                                s.getCursor());
                        bracketScopeStack.push(bracketScope);
                    } else {
                        // 嵌套的括号
                        BracketScope parent = bracketScopeStack.peek();
                        BracketScope bracketScope = new BracketScope(parent,
                                BracketScope.Type.Parenthesis,
                                s.getCursor());
                        parent.addChild(bracketScope);
                        bracketScopeStack.push(bracketScope);
                    }

                } else if (c == ')') {
                    // 右圆括号
                    if (bracketScopeStack == null || bracketScopeStack.isEmpty()) {
                        throw new ParseException("右圆括号不允许单独出现", whereClause, s.getCursor());
                    }
                    BracketScope bracketScope = bracketScopeStack.pop();
                    if (!bracketScope.isParenthesis()) {
                        throw new ParseException("圆括号与其他类型的括号不允许交叉嵌套", whereClause, s.getCursor());
                    }
                    bracketScope.setEndIndex(s.getCursor() + 1);
                    if (bracketScopes == null) {
                        bracketScopes = new LinkedList<>();
                    }
                    bracketScopes.offer(bracketScope);
                } else if (c == '[') {
                    // 左方括号
                    if (bracketScopeStack == null) {
                        bracketScopeStack = new Stack<>();
                    }
                    if (bracketScopeStack.isEmpty()) {
                        // 无嵌套的括号
                        BracketScope bracketScope = new BracketScope(null,
                                BracketScope.Type.Square,
                                s.getCursor());
                        bracketScopeStack.push(bracketScope);
                    } else {
                        // 嵌套的括号
                        BracketScope parent = bracketScopeStack.peek();
                        BracketScope bracketScope = new BracketScope(parent,
                                BracketScope.Type.Square,
                                s.getCursor());
                        bracketScopeStack.push(bracketScope);
                    }

                } else if (c == ']') {
                    // 右方括号
                    if (bracketScopeStack == null || bracketScopeStack.isEmpty()) {
                        throw new ParseException("右方括号不允许单独出现", whereClause, s.getCursor());
                    }
                    BracketScope bracketScope = bracketScopeStack.pop();
                    if (!bracketScope.isSquare()) {
                        throw new ParseException("方括号与其他类型的括号不允许交叉嵌套", whereClause, s.getCursor());
                    }
                    bracketScope.setEndIndex(s.getCursor() + 1);
                    if (bracketScopes == null) {
                        bracketScopes = new LinkedList<>();
                    }
                    bracketScopes.offer(bracketScope);
                } else if (c == '{') {
                    // 左花括号
                    if (bracketScopeStack == null) {
                        bracketScopeStack = new Stack<>();
                    }
                    if (bracketScopeStack.isEmpty()) {
                        // 无嵌套的括号
                        BracketScope bracketScope = new BracketScope(null,
                                BracketScope.Type.Curly,
                                s.getCursor());
                        bracketScopeStack.push(bracketScope);
                    } else {
                        // 嵌套的括号
                        BracketScope parent = bracketScopeStack.peek();
                        BracketScope bracketScope = new BracketScope(parent,
                                BracketScope.Type.Curly,
                                s.getCursor());
                        bracketScopeStack.push(bracketScope);
                    }

                } else if (c == '}') {
                    // 右花括号
                    if (bracketScopeStack == null || bracketScopeStack.isEmpty()) {
                        throw new ParseException("花方括号不允许单独出现", whereClause, s.getCursor());
                    }
                    BracketScope bracketScope = bracketScopeStack.pop();
                    if (!bracketScope.isCurly()) {
                        throw new ParseException("花括号与其他类型的括号不允许交叉嵌套", whereClause, s.getCursor());
                    }
                    bracketScope.setEndIndex(s.getCursor() + 1);
                    if (bracketScopes == null) {
                        bracketScopes = new LinkedList<>();
                    }
                    bracketScopes.offer(bracketScope);
                }
            }
            s.next();
        }

        if (bracketScopeStack != null && !bracketScopeStack.isEmpty()) {
            throw new ParseException("括号必须成对出现", whereClause, s.getCursor());
        }

        // 删除字符串两侧的空格和无用的括号
        StringStream stream = new StringStream(whereClause, beginIndex, endIndex,
                quotationScopes, bracketScopes);
        return trimAndRemoveSideBracket(stream);
    }

    /**
     * 去除无效的空格和引号
     *
     * 如 (a = "ABC" and b = 3)
     * 这个括号就是无效的
     *
     * @param stream
     * @return
     */
    private static StringStream trimAndRemoveSideBracket(@NotNull StringStream stream) {

        Collection<BracketScope> bracketScopes = stream.getBracketScopes();
        if (bracketScopes == null || bracketScopes.isEmpty()) {
            return stream.trim();
        }

        // 将字符串两边的空格和两侧无效的括号移除
        while (true) {
            stream = stream.trim();
            Iterator<BracketScope> iterator = bracketScopes.iterator();
            while (iterator.hasNext()) {
                BracketScope bracketScope = iterator.next();
                // 括号的范围已经完全包含了当前字符串的有效范围
                // 该括号是无效的
                if (bracketScope.containsTo(stream.getBeginIndex(), stream.getEndIndex())) {
                    stream = stream.subString(bracketScope.getBeginIndex() + 1,
                            stream.getEndIndex() - 1);
                    iterator.remove();
                    // 移除之后，需要让循环继续
                    break;
                } else {
                    return stream;
                }
            }
        }
    }

    /**
     * 移除已经失效的符号
     *
     * 如 a = "ABC" and b = 3
     * 若此时已经解析到了引号以后的位置，则认为引号已经失效了
     *
     * @param beginIndex
     * @param endIndex
     * @param scopes
     */
    private static void removeInvalidScope(int beginIndex, int endIndex,
                                           @NotNull Deque<? extends StringScope> scopes) {
        StringScope scope = scopes.peekFirst();
        while (scope != null) {
            if (scope.getEndIndex() <= beginIndex) {
                scopes.removeFirst();
            } else {
                break;
            }
        }
        scope = scopes.peekLast();
        while (scope != null) {
            if (scope.getBeginIndex() >= endIndex) {
                scopes.removeLast();
            } else {
                break;
            }
        }
    }

    private static boolean charIsNumber(char c) {
        for (int i = 0; i < number_char.length; i++) {
            if (c == number_char[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否不是一个键值
     *
     * 键必须以下划线和字母大头
     *
     * @param c
     * @return
     */
    private static boolean charIsNotKey(char c) {
        return charIsNumber(c);
    }

    private static LinearRing parseLinearRing(StringStream stream)
            throws ParseException {

        Collection<Coordinate> coordinates = new ArrayList<>();

        int beginIndex = stream.getBeginIndex();
        boolean begin = false;
        Queue<Double> coord = new LinkedList<>();
        while (stream.hasCurrent()) {
            char c = stream.getCurrent();
            if (c == ' ') {
                if (begin) {
                    Double dv = Double.valueOf(stream.subString(beginIndex,
                            stream.getCursor()).toString());
                    coord.add(dv);
                    begin = false;
                }
            } else if (c == ',') {
                if (begin) {
                    Double dv = Double.valueOf(stream.subString(beginIndex,
                            stream.getCursor()).toString());
                    coord.add(dv);
                }
                Coordinate coordinate = null;
                if (coord.size() == 2) {
                    coordinate = new Coordinate(coord.poll(), coord.poll());
                } else if (coord.size() == 3) {
                    coordinate = new Coordinate(coord.poll(), coord.poll(),
                            coord.poll());
                } else if (coord.size() == 4) {
                    coordinate = new Coordinate(coord.poll(), coord.poll(),
                            coord.poll(), coord.poll());
                } else {
                    throw new ParseException("坐标解析失败，不合理的坐标值数量（" + coord.size() + "）",
                            stream.getValue(), stream.getCursor());
                }
                coordinates.add(coordinate);
                begin = false;
            } else {
                if (!begin) {
                    beginIndex = stream.getCursor();
                    begin = true;
                }
            }
            stream.next();
        }

        if (begin) {
            Double dv = Double.valueOf(stream.subString(beginIndex,
                    stream.getCursor()).toString());
            coord.add(dv);
        }
        Coordinate coordinate = null;
        if (coord.size() == 2) {
            coordinate = new Coordinate(coord.poll(), coord.poll());
        } else if (coord.size() == 3) {
            coordinate = new Coordinate(coord.poll(), coord.poll(),
                    coord.poll());
        } else if (coord.size() == 4) {
            coordinate = new Coordinate(coord.poll(), coord.poll(),
                    coord.poll(), coord.poll());
        } else {
            throw new ParseException("坐标解析失败，不合理的坐标值数量（" + coord.size() + "）",
                    stream.getValue(), stream.getCursor());
        }
        coordinates.add(coordinate);

        if (coordinates.isEmpty()) {
            throw new ParseException("坐标为空",
                    stream.getValue(), stream.getCursor());
        }

        try {
            return GEOMETRY_FACTORY.createLinearRing(coordinates);
        } catch (Exception e) {
            throw new ParseException("坐标解析失败，坐标无法构成线环",
                    stream.getValue(), stream.getCursor());
        }
    }

    private static Polygon parsePolygon(StringStream stream)
            throws ParseException {

        Collection<LinearRing> linearRings = new ArrayList<>();
        while (stream.hasCurrent()) {
            BracketScope bracketScope = stream.inBracket();
            if (bracketScope != null) {
                if (!bracketScope.isParenthesis()) {
                    throw new ParseException("坐标解析失败",
                            stream.getValue(), stream.getCursor());
                }
                linearRings.add(parseLinearRing(stream.subString(
                        bracketScope.getBeginIndex() + 1,
                        bracketScope.getEndIndex() - 1)));
                stream.nextTo(bracketScope.getEndIndex());
                continue;
            }
            stream.next();
        }

        if (linearRings.isEmpty()) {
            throw new ParseException("坐标环为空",
                    stream.getValue(), stream.getCursor());
        }
        LinearRing shell = null;
        Collection<LinearRing> holes = null;
        for (LinearRing linearRing : linearRings) {
            if (shell == null) {
                shell = linearRing;
            } else {
                if (holes == null) {
                    holes = new ArrayList<>();
                }
                holes.add(linearRing);
            }
        }
        LinearRing[] _holes = (holes == null || holes.isEmpty()) ?
                null : holes.toArray(new LinearRing[holes.size()]);


        try {
            return GEOMETRY_FACTORY.createPolygon(shell, _holes);
        } catch (Exception e) {
            throw new ParseException("坐标解析失败，坐标无法构成多边形",
                    stream.getValue(), stream.getCursor());
        }
    }

    private static final char[] number_char = new char[] {
            '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9',
    };

    private static final char[] letter_char = new char[] {
            'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G',
            'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z',
    };

    private static final long MAX_INT_32 = Integer.MAX_VALUE;
    private static final long MIN_INT_32 = Integer.MIN_VALUE;

    private static final GeometryFactory GEOMETRY_FACTORY
            = FactoryFinder.getDefaultGeometryFactory();
}
