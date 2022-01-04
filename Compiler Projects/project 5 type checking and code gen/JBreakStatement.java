// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * An AST node for a break-statement.
 */
public class JBreakStatement extends JStatement {
    /**
     * Constructs an AST node for a break-statement.
     *
     * @param line line in which the break-statement occurs in the source file.
     */
    JStatement enclosingStatement;  //access to the stack
    String breaklabel;

    public JBreakStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        enclosingStatement = JMember.enclosingStatement.peek();
        //set the enclosed statements's hasBreak bool to true
        if (enclosingStatement instanceof JDoStatement){
            ((JDoStatement) enclosingStatement).setHasBreak(true);
        }
        if (enclosingStatement instanceof JSwitchStatement){
            ((JSwitchStatement) enclosingStatement).setHasBreak(true);
        }
        if (enclosingStatement instanceof JWhileStatement){
            ((JWhileStatement) enclosingStatement).setHasBreak(true);
        }
         if (enclosingStatement instanceof JForStatement){
            ((JForStatement) enclosingStatement).setHasBreak(true);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        if (enclosingStatement instanceof JDoStatement){
            breaklabel = ((JDoStatement) enclosingStatement).getBreakLabel();
        }
        if (enclosingStatement instanceof JForStatement){
            breaklabel = ((JForStatement) enclosingStatement).getBreakLabel();
        }
        if (enclosingStatement instanceof JWhileStatement){
            breaklabel = ((JWhileStatement) enclosingStatement).getBreakLabel();
        }
        //unconditional jump to break label using the getter in JStatement
        output.addBranchInstruction(GOTO, breaklabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JBreakStatement:" + line, e);
    }
}
