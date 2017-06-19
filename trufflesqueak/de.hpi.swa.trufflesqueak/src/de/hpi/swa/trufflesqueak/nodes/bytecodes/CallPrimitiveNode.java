package de.hpi.swa.trufflesqueak.nodes.bytecodes;

import java.util.List;
import java.util.Stack;

import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags.StatementTag;
import com.oracle.truffle.api.source.SourceSection;

import de.hpi.swa.trufflesqueak.exceptions.LocalReturn;
import de.hpi.swa.trufflesqueak.exceptions.PrimitiveFailed;
import de.hpi.swa.trufflesqueak.instrumentation.PrettyPrintVisitor;
import de.hpi.swa.trufflesqueak.model.CompiledCodeObject;
import de.hpi.swa.trufflesqueak.nodes.SqueakNode;
import de.hpi.swa.trufflesqueak.nodes.primitives.BuiltinPrimitive;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveNodeFactory;
import de.hpi.swa.trufflesqueak.nodes.primitives.QuickPrimitives.QuickReturnPrimitive;

public class CallPrimitiveNode extends SqueakBytecodeNode {
    @Child BuiltinPrimitive primitive;

    @SuppressWarnings("unused")
    public CallPrimitiveNode(CompiledCodeObject method, int idx, int i, int j) {
        super(method, idx);
        primitive = PrimitiveNodeFactory.forIdx(method, method.primitiveIndex());
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        assert index == 0;
        try {
            throw new LocalReturn(primitive.executeGeneric(frame));
        } catch (UnsupportedSpecializationException
                        | PrimitiveFailed
                        | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public void interpretOn(Stack<SqueakNode> stack, Stack<SqueakNode> statements) {
        // classic primitive method, takes its arguments from the frame and
        // returns
        if (method.hasPrimitive() && index == 0) {
            statements.push(this);
        } else {
            stack.push(this);
        }
    }

    @Override
    public int interpretOn(Stack<SqueakNode> stack, Stack<SqueakNode> statements, List<SqueakBytecodeNode> sequence) {
        super.interpretOn(stack, statements, sequence);
        if (index == 0) {
            for (int i = sequence.indexOf(this) + 1; i < sequence.size(); i++) {
                SqueakBytecodeNode node = sequence.get(i);
                if (node != null) {
                    if (node instanceof ExtendedStoreNode) {
                        // an error code is requested, we'll handle that
                        // eventually TODO: FIXME
                        stack.push(new ConstantNode(method, index,
                                        method.image.wrap("prim error codes not supported")));
                    }
                    break;
                }
            }
        }
        return sequence.indexOf(this) + 1;
    }

    @Override
    public void accept(PrettyPrintVisitor b) {
        b.visit(primitive);
    }

    @Override
    public boolean isReturn() {
        return primitive instanceof QuickReturnPrimitive;
    }

    @Override
    protected boolean isTaggedWith(Class<?> tag) {
        return tag == StatementTag.class;
    }

    @Override
    public void setSourceSection(SourceSection section) {
        super.setSourceSection(section);
        primitive.setSourceSection(section);
    }
}
