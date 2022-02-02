package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;


import java.util.*;

import org.apache.datasketches.theta.Sketch;
import org.apache.datasketches.theta.Intersection;
import org.apache.datasketches.theta.SetOperation;
import org.apache.datasketches.theta.AnotB;
import org.apache.datasketches.theta.Union;
import org.apache.datasketches.theta.Sketches;

public class ExpressionParserEvaluator {

    //order is important for operator precedence
    private final String [] operators = new String[]{"(", ")", "!", "&", "|"};

    public ExpressionParserEvaluator(HashMap<String, Sketch> operands) {
    }

    public ArrayList<String> parseExpression(String expr) {
        ArrayList<String> retVal = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (isCharOperator(c)) {
                if (token.length() > 0) {
                    retVal.add(token.toString());
                    token = new StringBuilder();
                }
                retVal.add(Character.toString(c));
            } else if (c == ' ') {
                if (token.length() > 0) {
                    retVal.add(token.toString());
                    token = new StringBuilder();
                }
            } else if (c == '\\') {
            } else {
                token.append(c);
            }
        }
        if (token.length() > 0) {
            retVal.add(token.toString());
        }
        return retVal;
    }

    public ArrayList<String> convertInfixToPostfix(ArrayList<String> tokens) throws Exception {
        ArrayList<String> retVal = new ArrayList<>();
        Stack<String> conversionStack = new Stack<>();
        int unclosedParenthesis = 0;
        for (String token : tokens) {
            if (isOperator(token)) {
                if (!token.equals(")")) {
                    while (!conversionStack.isEmpty()
                            && getOperatorPrecedence(conversionStack.peek()) < getOperatorPrecedence(token)
                            && !conversionStack.peek().equals("(")
                    ) {
                        retVal.add(conversionStack.pop());
                    }
                    conversionStack.push(token);
                } else { // == ")"
                    unclosedParenthesis--;
                    String popToken = conversionStack.pop();
                    if (popToken.equals("(")) {
                        unclosedParenthesis++;
                    }
                    while (!popToken.equals("(")) {
                        retVal.add(popToken);
                        if (!conversionStack.isEmpty()) {
                            popToken = conversionStack.pop();
                            if (popToken.equals("(")) {
                                unclosedParenthesis++;
                            }
                        } else {
                            break;
                        }
                    }
                }
            } else {
                retVal.add(token);
            }
            //For Debugging
            //for (int j = 0; j <= i; j++) {
                 //System.out.print(tokens.get(j) + " ");
            //}
            //System.out.print( " ~ " + token + " ~ ");
            //System.out.print(conversionStack.toString());
            //System.out.println( " ~ " + retVal.toString());
        }

        while(!conversionStack.isEmpty()) {
            String stackToken = conversionStack.pop();
            if (!stackToken.equals("(")) {
                retVal.add(stackToken);
            } else {
                unclosedParenthesis++;
            }
        }
        if (unclosedParenthesis != 0) {
            throw new Exception("unmatched brackets. " + unclosedParenthesis);
        }

        return retVal;
    }

    public Sketch evaluateExpr(ArrayList<String> expr,
                               HashMap<String, Sketch> mapOfDimValToSketch ) throws Exception{
        Stack<Sketch> conversionStack = new Stack<>();
        for (String token : expr) {
            if (!isOperator(token)) {
                if (!mapOfDimValToSketch.containsKey(token)) {
                    throw new Exception("Unknown operand " + token);
                }
                conversionStack.push(mapOfDimValToSketch.get(token));
            } else {
                if (conversionStack.isEmpty()) {
                    throw new Exception("Operator " + token + " cannot operate on zero operands");
                }
                if (token.equals("!")) {
                    Sketch s = conversionStack.pop();
                    AnotB not = SetOperation.builder().buildANotB();
                    not.setA(mapOfDimValToSketch.get(""));
                    not.notB(s);
                    conversionStack.push(not.getResult(true));
                } else {
                    if (token.equals("|") || token.equals("&")) {
                        Sketch sketchA = conversionStack.pop();
                        Sketch sketchB = conversionStack.pop();
                        if (token.equals("|")) {
                            Union union = SetOperation.builder().buildUnion();
                            union.union(sketchA);
                            union.union(sketchB);
                            conversionStack.push(union.getResult());
                        } else { //if (token.equals("&")) {
                            Intersection intersection = SetOperation.builder().buildIntersection();
                            intersection.intersect(sketchA); //, sketchB);
                            intersection.intersect(sketchB);
                            conversionStack.push(intersection.getResult());
                        }
                    } else {
                        throw new Exception("Unknown operator " + token + " found. ");
                    }
                }
            }
        }
        if (conversionStack.size() > 1) {
            throw new Exception("Grammar of the expression may be ambiguous");
        }
        return conversionStack.pop();
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

    private boolean isCharOperator(char operatorToTest) {
        boolean retVal = false;
        for (String operator : operators) {
            if (operator.equals(Character.toString(operatorToTest))) {
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

    public static Sketch evaluateExpression(String expr, HashMap<String, Sketch> mapOfDimValToSketch )
            throws Exception {
        ExpressionParserEvaluator expressionParserEvaluator = new ExpressionParserEvaluator(null);
        ArrayList<String> postfix = expressionParserEvaluator
                .convertInfixToPostfix(expressionParserEvaluator.parseExpression(expr));
        return expressionParserEvaluator.evaluateExpr(postfix,mapOfDimValToSketch);
    }

    public static void main(String [] args) throws Exception {
        ExpressionParserEvaluator expressionParserEvaluator = new ExpressionParserEvaluator(null);
        ArrayList<String> postfix = expressionParserEvaluator
                .convertInfixToPostfix(expressionParserEvaluator.parseExpression(args[0]));
        System.out.println(postfix);
        HashMap<String, Sketch> mapOfDimValToSketch = StaticUtils.readFile(args[1]);
        System.out.println(expressionParserEvaluator.evaluateExpr(postfix,mapOfDimValToSketch).getEstimate());
    }

    public static ArrayList<String> getOperands(String expr) {
        ExpressionParserEvaluator expressionParserEvaluator = new ExpressionParserEvaluator(null);
        ArrayList<String> retVal = expressionParserEvaluator.parseExpression(expr);
        retVal.removeAll(Arrays.asList(expressionParserEvaluator.operators));
        return retVal;
    }
}
