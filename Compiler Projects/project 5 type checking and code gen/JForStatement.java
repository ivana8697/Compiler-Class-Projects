// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a for-statement.
 */
class JForStatement extends JStatement {
    // Initialization.
    private ArrayList<JStatement> init;

    // Test expression
    private JExpression condition;

    // Update.
    private ArrayList<JStatement> update;

    // The body.
    private JStatement body;
    boolean hasBreak;  //for break statement
    String breakLabel;
    boolean hasContinue;  //for continue statement
    String continueLabel;

    /**
     * Constructs an AST node for a for-statement.
     *
     * @param line      line in which the for-statement occurs in the source file.
     * @param init      the initialization.
     * @param condition the test expression.
     * @param update    the update.
     * @param body      the body.
     */
    public JForStatement(int line, ArrayList<JStatement> init, JExpression condition,
                         ArrayList<JStatement> update, JStatement body) {
        super(line);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
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
    public JForStatement analyze(Context context) {
        JMember.enclosingStatement.push(this);  //push refrence to self on stack
        LocalContext forStatement = new LocalContext(context);
        //JMember.enclosingStatement.push(this);  //push refrence to self on stack
        if (init != null)
            for (int i = 0; i < init.size(); i++) {
                init.set(i, (JStatement) init.get(i).analyze(forStatement));
            }
        if (condition != null) {
            condition = (JExpression) condition.analyze(forStatement);
            condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        }
        if (update != null){
            for (int i = 0; i < update.size(); i++) {
                update.set(i, (JStatement) update.get(i).analyze(forStatement));
            }
        }
        if (body != null){
            body = (JStatement) body.analyze(forStatement);
        }
        //JMember.enclosingStatement.pop();  //pop off the stack
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String test = output.createLabel();
        String out = output.createLabel();
        if (hasBreak) {  //check if theres a break statement in the for loop
            breakLabel = output.createLabel();
        }
        if (hasContinue) {  //check if theres a break statement in the for loop
            continueLabel = output.createLabel();
        }
        if (init != null) {
            for (JStatement statements : init)
                statements.codegen(output);
        }
        output.addLabel(test);
        if (condition != null) {
            condition.codegen(output, out, false);
        }
        //body.codegen(output);
        if (body != null) {
            body.codegen(output);
        }
        if (hasContinue) {
            output.addLabel(continueLabel);  //add continue label
        }
        if (update != null) {
            for (JStatement statements : update)
            statements.codegen(output);
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
        json.addChild("JForStatement:" + line, e);
        if (init != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Init", e1);
            for (JStatement stmt : init) {
                stmt.toJSON(e1);
            }
        }
        if (condition != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Condition", e1);
            condition.toJSON(e1);
        }
        if (update != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Update", e1);
            for (JStatement stmt : update) {
                stmt.toJSON(e1);
            }
        }
        if (body != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Body", e1);
            body.toJSON(e1);
        }
    }
}
