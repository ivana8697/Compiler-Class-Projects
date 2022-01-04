// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a do-statement.
 */
public class JDoStatement extends JStatement {
    // Body.
    private JStatement body;

    // Test expression.
    private JExpression condition;
    boolean hasBreak;  //for break statement
    String breakLabel;
    boolean hasContinue;  //for continue statement
    String continueLabel;

    /**
     * Constructs an AST node for a do-statement.
     *
     * @param line      line in which the do-statement occurs in the source file.
     * @param body      the body.
     * @param condition test expression.
     */
    public JDoStatement(int line, JStatement body, JExpression condition) {
        super(line);
        this.body = body;
        this.condition = condition;
    }

    //setter for hasBreak variable
    public void setHasBreak(Boolean value) {
        this.hasBreak = value;
    }

    //getter for the BreakLabel
    public String getBreakLabel() {
        return breakLabel;
    }

    //setter for hasContinue variable
    public void setHasContinue(Boolean value) {
        this.hasContinue = value;
    }

    //getter for the continueLabel
    public String getContinueLabel() {
        return continueLabel;
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        JMember.enclosingStatement.push(this);  //push refrence to self on stack
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        body = (JStatement) body.analyze(context);
        JMember.enclosingStatement.push(this);  //pop off the stack
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String test = output.createLabel();
        if (hasBreak) {  //check if theres a break statement in the do while loop
            breakLabel = output.createLabel();
        }
        if (hasContinue) {
            continueLabel = output.createLabel();
        }
        String out = output.createLabel();
        output.addLabel(test);
        condition.codegen(output, out, false);
        body.codegen(output);
        if (hasContinue) {
            output.addLabel(continueLabel);  //add continue label
        }
        output.addBranchInstruction(GOTO, test);
        output.addLabel(out);
        if (hasBreak) {
            output.addLabel(breakLabel);  //add break label
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JDoStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Body", e1);
        body.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Condition", e2);
        condition.toJSON(e2);
    }
}
