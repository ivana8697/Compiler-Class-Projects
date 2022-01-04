// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * An AST node for a continue-statement.
 */
public class JContinueStatement extends JStatement {
    /**
     * Constructs an AST node for a continue-statement.
     *
     * @param line line in which the continue-statement occurs in the source file.
     */
    JStatement enclosingStatement;  //access to the stack
    String contLabel;

    public JContinueStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        enclosingStatement = JMember.enclosingStatement.peek();
        //set the enclosed statements's hasBreak bool to true using the setter
        if (enclosingStatement instanceof JDoStatement){
            ((JDoStatement) enclosingStatement).setHasContinue(true);
        }
        if (enclosingStatement instanceof JForStatement){
            ((JForStatement) enclosingStatement).setHasContinue(true);
        }
        if (enclosingStatement instanceof JWhileStatement){
            ((JWhileStatement) enclosingStatement).setHasContinue(true);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        if (enclosingStatement instanceof JDoStatement){
            String contLabel = ((JDoStatement) enclosingStatement).getContinueLabel();
            //unconditional jump to continue label using the getter in JStatement
            output.addBranchInstruction(GOTO, contLabel);
        }
        if (enclosingStatement instanceof JForStatement){
            String contLabel = ((JForStatement) enclosingStatement).getContinueLabel();
            //unconditional jump to continue label using the getter in JStatement
            output.addBranchInstruction(GOTO, contLabel);
        }
        if (enclosingStatement instanceof JWhileStatement){
            String contLabel = ((JWhileStatement) enclosingStatement).getContinueLabel();
            //unconditional jump to continue label using the getter in JStatement
            output.addBranchInstruction(GOTO, contLabel);
        }
        //unconditional jump to continue label using the getter in JStatement
        //output.addBranchInstruction(GOTO, contLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JContinueStatement:" + line, e);
    }
}
