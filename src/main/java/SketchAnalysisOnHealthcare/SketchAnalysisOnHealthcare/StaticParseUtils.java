package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.Stack;
import org.apache.datasketches.theta.Sketch;
import org.apache.datasketches.theta.UpdateSketch;
import org.apache.datasketches.theta.Intersection;
import org.apache.datasketches.theta.SetOperation;

public class StaticParseUtils {

    //order is important for operator precedence
    private final String [] operators = new String[]{"(", ")", "!", "&", "|"};
    private final HashMap<String, Sketch> operands;

    public StaticParseUtils(HashMap<String, Sketch> operands) {
        this.operands = operands;
    }

    public boolean parseExpression(String expr) {
        boolean retVal = true;


        return retVal;
    }

    public String cleanExpr(String expr) {
        StringBuilder retVal = new StringBuilder();

        return retVal.toString();
    }

    public ArrayList<String> convertInfixToPostfix(String expr) {
        ArrayList<String> retVal = new ArrayList<>();
        Stack<String> conversionStack = new Stack<>();
        boolean unclosedParenthesis = false;
        String [] tokens = expr.split(" ", -1);
        for (String token : tokens) {
            if (isOperator(token)) {
                if (!token.equals(")")) {
                    unclosedParenthesis = true;
                    while (!conversionStack.isEmpty()
                            && getOperatorPrecedence(conversionStack.peek()) < getOperatorPrecedence(token)
                            && !conversionStack.peek().equals("(")
                    ) {
                        retVal.add(conversionStack.pop());
                    }
                    conversionStack.push(token);
                } else {
                    String popToken = conversionStack.pop();
                    while (!popToken.equals("(") && unclosedParenthesis) {
                        retVal.add(popToken);
                        if (!conversionStack.isEmpty()) {
                            popToken = conversionStack.pop();
                        } else {
                            break;
                        }
                    }
                    unclosedParenthesis = false;
                }
            } else {
                retVal.add(token);
            }
            //For Debugging
            //            for (int j = 0; j <= i; j++) {
            //                System.out.print(tokens[j] + " ");
            //            }
            //            System.out.print( " ~ " + token + " ~ ");
            //            System.out.print(conversionStack.toString());
            //            System.out.println( " ~ " + retVal.toString());
        }

        while(!conversionStack.isEmpty()) {
            retVal.add(conversionStack.pop());
        }

        return retVal;
    }

    public Sketch evaluateExpr(ArrayList<String> expr) {
        Sketch retVal = null;
        Stack<String> conversionStack = new Stack<>();

        return retVal;
    }

    private boolean isOperator(String token) {
        boolean retVal = false;
        for (String operator : operators) {
            if (operator.equals(token)) {
                retVal = true;
                break;
            }
        }
        return retVal;
    }

    private int getOperatorPrecedence (String operator) {
        for (int i = 0; i < operators.length; i++) {
            if (operator.equals(operators[i])) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String [] args) {
        StaticParseUtils staticParseUtils = new StaticParseUtils(null);
        System.out.println("Got  " + staticParseUtils.convertInfixToPostfix(args[0]));
        //ToDo: refactor reading a file with sketches
        // ReadInputAndWriteToOutput.readInputFile(args[1],
        //System.out.println("Want ABC&DE&|&F|");
    }

}
