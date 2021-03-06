package de.hpi.swa.trufflesqueak.nodes.primitives.impl;

import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Instrumentable;

import de.hpi.swa.trufflesqueak.exceptions.PrimitiveFailed;
import de.hpi.swa.trufflesqueak.model.CompiledMethodObject;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveNode;

@Instrumentable(factory = PrimDebuggerWrapper.class)
public class PrimDebugger extends PrimitiveNode {
    protected PrimDebugger(PrimDebugger pm) {
        super((CompiledMethodObject) pm.method);
    }

    public PrimDebugger(CompiledMethodObject cm) {
        super(cm);
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        throw new PrimitiveFailed();
    }

    @Override
    protected boolean isTaggedWith(Class<?> tag) {
        return tag == DebuggerTags.AlwaysHalt.class;
    }
}
