import Exceptions.HDLException;
import Exceptions.HDLParseException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;;

/**
 * @author Jonah Tharakan
 *
 * Class that represents one expression between any number of signals and
 * contants.  Expressions use logical or arithmetical operators to combine
 * signals and constants to resolve a signal integer value, which will be
 * returned upon calling "eval(...)" on the Expression object.
 *
 * Internally the expressions are just stored as strings.
 *
 * Valid expressions take one of the following forms:
 * - #
 * - x
 * - ~E
 * - !E
 * - (E OP E)
 * where # represents a constant, x represents a signal name, E represents
 * another expression, and OP represents a valid binary operator.
 */

public class Expression {

    public static final List<String> VALID_OPERATORS = Arrays.asList(
            "+", "-", "&", "|", "^", "==", "!="
    );

    private final String expression;

    public Expression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return this.expression;
    }

    /**
     * Evaluates this expression to return a single integer.
     * Any encountered signals are replaced with their value found in VALUES.
     *
     * Throws HDLParseException if there is a problem with the expression.
     */
    public int eval(HashMap<String, Integer> values) throws HDLParseException {
        char firstChar = expression.charAt(0);

        // Not case
        if (firstChar == '!') {
            return evalLogicalNot(values);
        }
        else if (firstChar == '~') {
            return evalBitwiseNot(values);
        }

        // Subexpression case
        else if (expression.contains("(")) {
            return evalOperator(values);
        }

        // Variable lookup case
        else if (Character.isLetter(expression.charAt(0))) {
            if (values.containsKey(expression)) {
                return values.get(expression);
            } else {
                String msg = String.format("Provided values map does not contain key %s!", expression);
                throw new HDLParseException(msg);
            }
        }

        // Constant case
        else {
            if (expression.length() > 2 && expression.startsWith("0x")) {
                return Integer.parseInt(expression.substring(2), 16);
            } else if (expression.length() > 2 && expression.startsWith("0b")) {
                return Integer.parseInt(expression.substring(2), 2);
            }
            return Integer.parseInt(expression);
        }
    }

    /**
     * Evaluates this expression to return a single integer.
     * Cannot be used to evaluate expressions with signals.
     *
     * Throws HDLParseException if there is a problem with the expression.
     */
    public int eval() throws HDLParseException {
        return eval(new HashMap<>());
    }


    // PRIVATE HELPER METHODS /////////////////////////////////////////////////

    /**
     * Evaluates the subexpression and then returns the logical not (!)
     * of the subexpression value
     */
    private int evalLogicalNot(HashMap<String, Integer> values) {
        Expression subexpr = new Expression(expression.substring(1));
        return subexpr.eval(values) == 0 ? 1 : 0;
    }

    /**
     * Evaluates the subexpression and then returns the bitwise not (~)
     * of the subexpression value
     */
    private int evalBitwiseNot(HashMap<String, Integer> values) {
        Expression subexpr = new Expression(expression.substring(1));
        return ~subexpr.eval(values);
    }

    /**
     * Splits the expression into two subexpressions. Evaluates each of them
     * and then uses the operator to combine those outputs, returning that
     * final output.
     *
     * Expression must take this form:
     * (subexpr1 OP subexpr2)
     * where subexpr1/2 can be a not expression, number, variable, or operator expression
     *
     * Throws HDLParseException if there is no operator or an invalid operator.
     */
    private int evalOperator(HashMap<String, Integer> values) throws HDLParseException {
        // Use iteration with a counter of open/closed parentheses to find out where the two sub expressions are
        // We know that the operator applying in this expression is the one that
        // first comes after we have seen one more open parenthesis than closed
        String subexpr1str = "";
        String op = "";
        String subexpr2str = "";

        int openMinusClose = 0;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') openMinusClose += 1;
            else if (c == ')') openMinusClose -= 1;
            else if (openMinusClose == 1) {
                if (VALID_OPERATORS.contains("" + c)) {
                    // Here we have found the operator
                    subexpr1str = expression.substring(1, i).trim();
                    op = expression.substring(i, i + 1);
                    subexpr2str = expression.substring(i + 1, expression.length() - 1).trim();
                }
                // Length 2 operators check
                else if (i < expression.length() - 1 && VALID_OPERATORS.contains(""+c+expression.charAt(i+1))) {
                    // Here we have found the operator
                    subexpr1str = expression.substring(1, i).trim();
                    op = expression.substring(i, i + 2);
                    subexpr2str = expression.substring(i + 2, expression.length() - 1).trim();
                }
            }
        }

        if (subexpr1str.equals("") || subexpr2str.equals("") || op.equals("")) {
            throw new HDLParseException("Either no or an invalid operator was provided!");
        }

        Expression subexpr1 = new Expression(subexpr1str);
        Expression subexpr2 = new Expression(subexpr2str);

        // Figure out operator and pass to appropriate function
        switch (op) {
            case "+":
                return evalPlus(subexpr1, subexpr2, values);
            case "-":
                return evalMinus(subexpr1, subexpr2, values);
            case "&":
                return evalBitwiseAnd(subexpr1, subexpr2, values);
            case "|":
                return evalBitwiseOr(subexpr1, subexpr2, values);
            case "^":
                return evalBitwiseXor(subexpr1, subexpr2, values);
            case "==":
                return evalEquality(subexpr1, subexpr2, values);
            case "!=":
                return evalInequality(subexpr1, subexpr2, values);


            default:
                throw new HDLParseException("Either no or an invalid operator was provided!");
        }

    }


    // BINARY FUNCTIONS

    private int evalPlus(Expression subexpr1, Expression subexpr2, HashMap<String, Integer> values) {
        return subexpr1.eval(values) + subexpr2.eval(values);
    }

    private int evalMinus(Expression subexpr1, Expression subexpr2, HashMap<String, Integer> values) {
        return subexpr1.eval(values) - subexpr2.eval(values);
    }

    private int evalBitwiseAnd(Expression subexpr1, Expression subexpr2, HashMap<String, Integer> values) {
        return subexpr1.eval(values) & subexpr2.eval(values);
    }

    private int evalBitwiseOr(Expression subexpr1, Expression subexpr2, HashMap<String, Integer> values) {
        return subexpr1.eval(values) | subexpr2.eval(values);
    }

    private int evalBitwiseXor(Expression subexpr1, Expression subexpr2, HashMap<String, Integer> values) {
        return subexpr1.eval(values) ^ subexpr2.eval(values);
    }

    private int evalEquality(Expression subexpr1, Expression subexpr2, HashMap<String, Integer> values) {
        return subexpr1.eval(values) == subexpr2.eval(values) ? 1 : 0;
    }

    private int evalInequality(Expression subexpr1, Expression subexpr2, HashMap<String, Integer> values) {
        return subexpr1.eval(values) == subexpr2.eval(values) ? 0 : 1;
    }


    // OTHER INTERFACE METHODS ////////////////////////////////////////////////

    /**
     * Returns a set of all signal names in this expression.
     *
     * Throws HDLParseException if there is a problem with the expression.
     */
    public HashSet<String> getSignalNames() throws HDLParseException {
        char firstChar = expression.charAt(0);

        // Not case
        if (firstChar == '!' || firstChar == '~') {
            return (new Expression(expression.substring(1))).getSignalNames();
        }

        // Subexpression case
        else if (expression.contains("(")) {
            return getSignalNamesOperator();
        }

        // Variable lookup case
        else if (Character.isLetter(expression.charAt(0))) {
            HashSet<String> signal = new HashSet<>();
            signal.add(expression);
            return signal;
        }

        // Constant case
        else {
            return new HashSet<>();
        }
    }

    /**
     * Returns a set of all signal names in this expression in the case of
     * an operator expression.
     *
     * Throws HDLParseException if there is a problem with the expression.
     */
    private HashSet<String> getSignalNamesOperator() throws HDLParseException {
        // Use iteration with a counter of open/closed parentheses to find out where the two sub expressions are
        // We know that the operator applying in this expression is the one that
        // first comes after we have seen one more open parenthesis than closed
        String subexpr1str = "";
        String subexpr2str = "";

        int openMinusClose = 0;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') openMinusClose += 1;
            else if (c == ')') openMinusClose -= 1;
            else if (openMinusClose == 1) {
                if (VALID_OPERATORS.contains("" + c)) {
                    // Here we have found the operator
                    subexpr1str = expression.substring(1, i).trim();
                    subexpr2str = expression.substring(i + 1, expression.length() - 1).trim();
                }
                // Length 2 operators check
                else if (i < expression.length() - 1 && VALID_OPERATORS.contains(""+c+expression.charAt(i+1))) {
                    // Here we have found the operator
                    subexpr1str = expression.substring(1, i).trim();
                    subexpr2str = expression.substring(i + 2, expression.length() - 1).trim();
                }
            }
        }

        if (subexpr1str.equals("") || subexpr2str.equals("")) {
            throw new HDLParseException("Either no or an invalid operator was provided!");
        }

        Expression subexpr1 = new Expression(subexpr1str);
        Expression subexpr2 = new Expression(subexpr2str);

        HashSet<String> signals = subexpr1.getSignalNames();
        signals.addAll(subexpr2.getSignalNames());
        return signals;
    }


}
