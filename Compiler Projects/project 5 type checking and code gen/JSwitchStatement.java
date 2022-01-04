// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a switch-statement.
 */
public class JSwitchStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    // List of switch-statement groups.
    private ArrayList<SwitchStatementGroup> stmtGroup;
    private int lo;
    private int hi;
    private int nLabels;
    boolean hasBreak;  //for break statement
    String breakLabel;

    /**
     * Constructs an AST node for a switch-statement.
     *
     * @param line      line in which the switch-statement occurs in the source file.
     * @param condition test expression.
     * @param stmtGroup list of statement groups.
     */
    public JSwitchStatement(int line, JExpression condition,
                            ArrayList<SwitchStatementGroup> stmtGroup) {
        super(line);
        this.condition = condition;
        this.stmtGroup = stmtGroup;
    }

    //setter for hasBreak variable
    public void setHasBreak(Boolean value) {
        this.hasBreak = value;
    }

    //getter for the BreakLabel
    public String getBreakLabel() {
        return breakLabel;
    }

    private int decideSwitch() {
        long tableSpaceCost = 5 + hi - lo;
        long tableTimeCost = 3;
        long lookupSpaceCost = 3 + 2 * nLabels;
        long lookupTimeCost = nLabels;
        int opcode = nLabels > 0 && (tableSpaceCost + 3 * tableTimeCost <= lookupSpaceCost
            + 3 * lookupTimeCost) ? TABLESWITCH : LOOKUPSWITCH;
        return opcode;
    }



    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) { 
        JMember.enclosingStatement.push(this);  //push refrence to self on stack
        condition = (JExpression) condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.INT);  //must be int
        ArrayList<JExpression> switchlabels = new ArrayList<>();
        nLabels = 0;
        /*for (SwitchStatementGroup group : stmtGroups) {
            LocalContext switchcontext = new LocalContext(context);
            for (int i = 0; i < group.getSwitchLabels().size(); i++) {
                JExpression label = group.getSwitchLabels().get(i);
                if (label != null) {
                    nLabels++;
                    group.getSwitchLabels().set(i, (JLiteralInt) label.analyze(switchcontext));
                    label.type().mustMatchExpected(line(), Type.INT);
                    switchlabels.add(label);
                }
            } for (int i = 0; i < group.statements().size(); i++) {
                JStatement statement = group.statements().get(i);
                group.getStatements().set(i, (JStatement) statement.analyze(switchcontext));
            }
        }
        //find lo and hi
        lo = hi = ((JLiteralInt) switchlabels.get(0));
        for (int i = 0; i < switchlabels.size(); i++) {
            int temp = ((JLiteralInt) switchlabels.get(i));
            if (hi < temp) {
                hi = temp;
            }
            if (lo > temp) {
                lo = temp;
            }
        }*/
        //JMember.enclosingStatement.push(this);  //pop off the stack
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
      /*  String defaultCase = output.createLabel();
        if (hasBreak) {  //check if theres a break statement in the for loop
            breakLabel = output.createLabel();
        }
        condition.codegen(output);
        int opcode = decideSwitch();
        boolean hasDefault = false;
        if (opcode == TABLESWITCH) {
            //make array of strings that CLEmitter method needs
            ArrayList<String> labels = new ArrayList<>();
            for (SwitchStatementGroup group : stmtGroup) {
                ArrayList<JExpression> sLabels = group.switchLables;
                for (JExpression switchLabel : sLabels) {
                    if (switchLabel != null) {
                        int casenum = ((JLiteralInt) switchLabel).getInt();
                        //add text to differentiate if case numbers are same
                        labels.add("tableswitch" + casenum);
                    }
                }
            }
            output.addTABLESWITCHInstruction(defaultCase, lo, hi, labels);
        }
        if (opcode == LOOKUPSWITCH) {
            //make TreeMap for CLEmitter method and num of pairs
            TreeMap<Integer, String> matchLabelPairs = new TreeMap<Integer, String>();
            for (SwitchStatementGroup group : stmtGroup) {
                ArrayList<JExpression> sLabels = group.switchLabels;
                for (JExpression switchLabel : switchLabels) {
                    if (switchLabel != null) {
                        int casenum = ((JLiteralInt) switchLabel).getInt();
                        //add text to differentiate if case numbers are same
                        matchLabelPairs.put(literal, "lookup" + casenum);
                    }
                }
            }
            output.addLOOKUPSWITCHInstruction(defaultCase, matchLabelPairs.size(), matchLabelPairs);
        }
        if (!hasDefault) {
            output.addLabel(defaultLabel);
        }
        output.addLabel(breakLabel); */
    }
    
    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JSwitchStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        for (SwitchStatementGroup group : stmtGroup) {
            group.toJSON(e);
        }
    }
}

/**
 * A switch statement group consists of case labels and a block of statements.
 */
class SwitchStatementGroup {
    // Case labels.
    private ArrayList<JExpression> switchLabels;

    // Block of statements.
    private ArrayList<JStatement> block;

    /**
     * Constructs a switch-statement group.
     *
     * @param switchLabels case labels.
     * @param block        block of statements.
     */
    public SwitchStatementGroup(ArrayList<JExpression> switchLabels, ArrayList<JStatement> block) {
        this.switchLabels = switchLabels;
        this.block = block;
    }

    /**
     * Stores information about this switch statement group in JSON format.
     *
     * @param json the JSON emitter.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("SwitchStatementGroup", e);
        for (JExpression label : switchLabels) {
            JSONElement e1 = new JSONElement();
            if (label != null) {
                e.addChild("Case", e1);
                label.toJSON(e1);
            } else {
                e.addChild("Default", e1);
            }
        }
        if (block != null) {
            for (JStatement stmt : block) {
                stmt.toJSON(e);
            }
        }
    }
}
