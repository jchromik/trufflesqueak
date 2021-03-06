package de.hpi.swa.trufflesqueak.nodes.bytecodes;

import com.oracle.truffle.api.frame.VirtualFrame;

import de.hpi.swa.trufflesqueak.model.CompiledCodeObject;
import de.hpi.swa.trufflesqueak.nodes.SqueakNode;
import de.hpi.swa.trufflesqueak.nodes.context.FrameSlotReadNode;

public abstract class RemoteTempBytecodeNode extends SqueakBytecodeNode {
    @Child SqueakNode execNode;

    public RemoteTempBytecodeNode(CompiledCodeObject cm, int idx) {
        super(cm, idx);
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return execNode.executeGeneric(frame);
    }

    protected static SqueakNode getTempArray(CompiledCodeObject cm, int indexOfArray) {
        return FrameSlotReadNode.temp(cm, indexOfArray);
    }
}
