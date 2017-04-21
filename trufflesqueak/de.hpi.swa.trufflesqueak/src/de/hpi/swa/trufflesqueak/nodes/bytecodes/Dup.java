package de.hpi.swa.trufflesqueak.nodes.bytecodes;

import com.oracle.truffle.api.frame.VirtualFrame;

import de.hpi.swa.trufflesqueak.model.BaseSqueakObject;
import de.hpi.swa.trufflesqueak.model.CompiledMethodObject;
import de.hpi.swa.trufflesqueak.nodes.SqueakBytecodeNode;

public class Dup extends SqueakBytecodeNode {

    public Dup(CompiledMethodObject cm, int idx) {
        super(cm, idx);
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object top = top(frame);
        push(frame, top);
        return top;
    }

}