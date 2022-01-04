// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a try-catch-finally statement.
 */
class JTryStatement extends JStatement {
    // The try block.
    private JBlock tryBlock;

    // The catch parameters.
    private ArrayList<JFormalParameter> parameters;

    // The catch blocks.
    private ArrayList<JBlock> catchBlocks;

    // The finally block.
    private JBlock finallyBlock;

    /**
     * Constructs an AST node for a try-statement.
     *
     * @param line         line in which the while-statement occurs in the source file.
     * @param tryBlock     the try block.
     * @param parameters   the catch parameters.
     * @param catchBlocks  the catch blocks.
     * @param finallyBlock the finally block.
     */
    public JTryStatement(int line, JBlock tryBlock, ArrayList<JFormalParameter> parameters,
                         ArrayList<JBlock> catchBlocks, JBlock finallyBlock) {
        super(line);
        this.tryBlock = tryBlock;
        this.parameters = parameters;
        this.catchBlocks = catchBlocks;
        this.finallyBlock = finallyBlock;
    }

    /**
     * {@inheritDoc}
     */
    public JTryStatement analyze(Context context) {
        LocalContext tryContext = new LocalContext(context);
        tryBlock = (JBlock) tryBlock.analyze(tryContext);
        for (int i = 0; i < parameters.size(); i++) {
            LocalContext catchParams = new LocalContext(context);
            JFormalParameter params = parameters.get(i);
            params.setType(params.type().resolve(catchParams));
            Type type = params.type();
            int offset = catchParams.nextOffset();
            LocalVariableDefn defn = new LocalVariableDefn(type, offset);
            defn.initialize();
            catchParams.addEntry(params.line(), params.name(), defn);
            catchBlocks.set(i, ((JBlock) catchBlocks.get(i)).analyze(catchParams));
        }

        if (finallyBlock != null) {
            finallyBlock = (JBlock) finallyBlock.analyze(context);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String startTry = output.createLabel();
        String endTry = output.createLabel();
        //String startCatch = output.createLabel();
        String endCatch = output.createLabel();
        String startFinally = output.createLabel();
        String startFinallyPlusOne = output.createLabel();
        String endFinally = output.createLabel();

        output.addLabel(startTry);
        tryBlock.codegen(output);
        if (finallyBlock != null) {  //check null since its optional to use fianlly
            finallyBlock.codegen(output);
        }
        output.addBranchInstruction(GOTO, endFinally);
        output.addLabel(endTry);
        //for each catch block
        ArrayList<String> catchLabel = new ArrayList<String>();
        for (int i = 0; i < catchBlocks.size(); i++) {
            String startCatch = output.createLabel();
            catchLabel.add(startCatch);
            output.addExceptionHandler(startTry, endTry, startCatch,
                 parameters.get(i).type().jvmName());
            JBlock catchBlock = catchBlocks.get(i);
            output.addLabel(startCatch);
            output.addNoArgInstruction(ASTORE_1);
            catchBlock.codegen(output);
            if (finallyBlock != null) {
                finallyBlock.codegen(output);
            }
            output.addBranchInstruction(GOTO, endFinally);
        }
        output.addExceptionHandler(startTry, endTry, startFinally, null);
        output.addLabel(startFinally);
        if (finallyBlock != null) {
            output.addOneArgInstruction(ASTORE, catchLabel.size() + 2);
            output.addLabel(startFinallyPlusOne);
            finallyBlock.codegen(output);
            output.addOneArgInstruction(ALOAD, catchLabel.size() + 2);
            output.addNoArgInstruction(ATHROW);
        }
        output.addLabel(endFinally);
        for (int i = 0; i < catchLabel.size(); i++) {
            if (i < catchLabel.size() - 1) {
                output.addExceptionHandler(catchLabel.get(i), catchLabel.get(i + 1), startFinally, null);
            } else {
                output.addExceptionHandler(catchLabel.get(i), startFinally, startFinally, null);
            }
        }
        if (finallyBlock != null) {
            output.addExceptionHandler(startFinally, startFinallyPlusOne, startFinally, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JTryStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("TryBlock", e1);
        tryBlock.toJSON(e1);
        if (catchBlocks != null) {
            for (int i = 0; i < catchBlocks.size(); i++) {
                JFormalParameter param = parameters.get(i);
                JBlock catchBlock = catchBlocks.get(i);
                JSONElement e2 = new JSONElement();
                e.addChild("CatchBlock", e2);
                String s = String.format("[\"%s\", \"%s\"]", param.name(), param.type() == null ?
                        "" : param.type().toString());
                e2.addAttribute("parameter", s);
                catchBlock.toJSON(e2);
            }
        }
        if (finallyBlock != null) {
            JSONElement e2 = new JSONElement();
            e.addChild("FinallyBlock", e2);
            finallyBlock.toJSON(e2);
        }
    }
}
