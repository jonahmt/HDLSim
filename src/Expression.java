import java.util.HashMap;

public class Expression {

    private String expression;

    public Expression(String expression) {
        this.expression = expression;
    }

    public int eval(HashMap<String, Integer> values) {
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

        }

        // Variable lookup case
        else if (MyUtils.isAlphabetical(expression.charAt(0))) {
            return values.get(expression);
        }

        // Constant case
        else {
            return Integer.parseInt(expression);
        }

        return 0;
    }

    public int eval() {
        return eval(new HashMap<>());
    }


    // PRIVATE HELPER METHODS ///////////////////////////////////////////

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


}
