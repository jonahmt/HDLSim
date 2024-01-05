import java.util.HashMap;

public class Expression {

    private String expression;

    public Expression(String expression) {
        this.expression = expression;
    }

    public int eval(HashMap<String, Integer> values) {
        // Subexpression case
        if (expression.contains("(")) {

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
}
