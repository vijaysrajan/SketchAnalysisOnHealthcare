package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

import java.util.ArrayList;

public class DecisionTreeNode {
    private String rule;
    private double goodCnt;
    private double badCnt;
    private double total;
    private double badCntDividedByTotal;

    public DecisionTreeNode(String rule, double goodCnt,
                            double badCnt, double total, DecisionTreeNode parent) {
        this.rule = rule;
        this.goodCnt = goodCnt;
        this.badCnt = badCnt;
        this.total = total;
        this.badCntDividedByTotal = badCnt / total;
        this.parent = parent;
    }

    public String getRule() {
        return rule;
    }

    public double getGoodCnt() {
        return goodCnt;
    }

    public double getBadCnt() {
        return badCnt;
    }

    public double getTotal() {
        return total;
    }

    public double getBadCntDividedByTotal() {
        return badCntDividedByTotal;
    }

    ArrayList<DecisionTreeNode> children = new ArrayList<>();
    DecisionTreeNode parent;
    ArrayList<DecisionTreeNode> siblings = new ArrayList<>();

    public void addChild(DecisionTreeNode child) {
        children.add(child);
        child.parent = this;
    }
    public void fillSiblings () {
        if (parent != null) {
            for (DecisionTreeNode sibling :parent.children) {
                if (this != sibling) {
                    siblings.add(sibling);
                }
            }
        }
    }
}
