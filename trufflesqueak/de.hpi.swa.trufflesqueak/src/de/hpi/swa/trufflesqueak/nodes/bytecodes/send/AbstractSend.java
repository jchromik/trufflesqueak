package de.hpi.swa.trufflesqueak.nodes.bytecodes.send;

import java.util.List;
import java.util.Stack;
import java.util.Vector;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.ValueProfile;

import de.hpi.swa.trufflesqueak.instrumentation.PrettyPrintVisitor;
import de.hpi.swa.trufflesqueak.model.ClassObject;
import de.hpi.swa.trufflesqueak.model.CompiledCodeObject;
import de.hpi.swa.trufflesqueak.nodes.DispatchNode;
import de.hpi.swa.trufflesqueak.nodes.DispatchNodeGen;
import de.hpi.swa.trufflesqueak.nodes.LookupNode;
import de.hpi.swa.trufflesqueak.nodes.LookupNodeGen;
import de.hpi.swa.trufflesqueak.nodes.SqueakNode;
import de.hpi.swa.trufflesqueak.nodes.SqueakTypesGen;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.DupNode;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.SqueakBytecodeNode;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.jump.ConditionalJump;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.jump.IfNilCheck;
import de.hpi.swa.trufflesqueak.nodes.context.SqueakLookupClassNode;
import de.hpi.swa.trufflesqueak.nodes.context.SqueakLookupClassNodeGen;

public abstract class AbstractSend extends SqueakBytecodeNode {
    private final ValueProfile classProfile = ValueProfile.createClassProfile();
    public final Object selector;
    @Child public SqueakNode receiverNode;
    @Child protected SqueakLookupClassNode lookupClassNode;
    @Children public final SqueakNode[] argumentNodes;
    @Child private LookupNode lookupNode;
    @Child private DispatchNode dispatchNode;

    public AbstractSend(CompiledCodeObject method, int idx, Object sel, int argcount) {
        super(method, idx);
        selector = sel;
        argumentNodes = new SqueakNode[argcount];
        lookupClassNode = SqueakLookupClassNodeGen.create(method);
        dispatchNode = DispatchNodeGen.create();
        lookupNode = LookupNodeGen.create();
    }

    protected AbstractSend(CompiledCodeObject method, int idx, Object sel, SqueakNode[] argNodes) {
        super(method, idx);
        selector = sel;
        argumentNodes = argNodes;
        lookupClassNode = SqueakLookupClassNodeGen.create(method);
        dispatchNode = DispatchNodeGen.create();
        lookupNode = LookupNodeGen.create();
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object receiver = classProfile.profile(receiverNode.executeGeneric(frame));
        return executeSend(frame, receiver);
        // TODO: OaM
    }

    @ExplodeLoop
    public Object executeSend(VirtualFrame frame, Object receiver) {
        ClassObject rcvrClass;
        try {
            rcvrClass = SqueakTypesGen.expectClassObject(lookupClassNode.executeLookup(receiver));
        } catch (UnexpectedResultException e) {
            throw new RuntimeException("receiver has no class");
        }
        CompilerAsserts.compilationConstant(argumentNodes.length);
        Object[] arguments = new Object[argumentNodes.length + 1];
        arguments[0] = receiver;
        for (int i = 0; i < argumentNodes.length; i++) {
            arguments[i + 1] = argumentNodes[i].executeGeneric(frame);
        }
        CompilerAsserts.compilationConstant(argumentNodes.length);
        Object lookupResult = lookupNode.executeLookup(rcvrClass, selector);
        return dispatchNode.executeDispatch(lookupResult, arguments);
    }

    @SuppressWarnings("static-method")
    private boolean isCascadeFlag(SqueakNode rcvr) {
        return rcvr instanceof DupNode;
    }

    private boolean isCaseMacro(SqueakNode rcvr, List<SqueakBytecodeNode> sequence) {
        return isCascadeFlag(rcvr) && selector == method.image.eq && willJumpIf(sequence, false);
    }

    private boolean isIfNil(SqueakNode rcvr, List<SqueakBytecodeNode> sequence) {
        return isCascadeFlag(rcvr) && selector == method.image.equivalent && willJumpIf(sequence, false);
    }

    private boolean isIfNotNil(SqueakNode rcvr, List<SqueakBytecodeNode> sequence) {
        return isCascadeFlag(rcvr) && selector == method.image.equivalent && willJumpIf(sequence, true);
    }

    private boolean mayBeCascade(SqueakNode rcvr) {
        return isCascadeFlag(rcvr);
    }

    private boolean willJumpIf(List<SqueakBytecodeNode> sequence, boolean flag) {
        for (int i = sequence.indexOf(this) + 1; i < sequence.size(); i++) {
            SqueakBytecodeNode node = sequence.get(i);
            if (node != null) {
                if ((node instanceof ConditionalJump) && ((ConditionalJump) node).isIfTrue == flag) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public int interpretOn(Stack<SqueakNode> stack, Stack<SqueakNode> statements, List<SqueakBytecodeNode> sequence) {
        for (int i = argumentNodes.length - 1; i >= 0; i--) {
            argumentNodes[i] = stack.pop();
        }
        receiverNode = stack.pop();
        if (isCaseMacro(receiverNode, sequence)) {
            statements.push(argumentNodes[0]);
            stack.push(receiverNode); // restore cascade flag
        } else if (isIfNil(receiverNode, sequence)) {
            stack.pop(); // remove duplicate cascade flag
            receiverNode = stack.pop();
            stack.push(new IfNilCheck(method, receiverNode, true));
        } else if (isIfNotNil(receiverNode, sequence)) {
            stack.pop(); // remove duplicate cascade flag
            receiverNode = stack.pop();
            stack.push(new IfNilCheck(method, receiverNode, false));
        } else if (mayBeCascade(receiverNode)) {
            if (isCascadeFlag(stack.peek())) {
                // we're not the last cascade message
                stack.push(this);
            } else {
                int preCascadeStatementIdx = ((DupNode) receiverNode).getStatementsIdx();
                List<SqueakNode> cascadedSends = new Vector<>(statements.subList(preCascadeStatementIdx,
                                statements.size()));
                statements.setSize(preCascadeStatementIdx);
                receiverNode = stack.pop();
                stack.push(new CascadedSend(method,
                                index,
                                receiverNode,
                                selector,
                                argumentNodes,
                                cascadedSends.toArray(new SqueakNode[0])));
            }
        } else {
            interpretOn(stack, statements);
        }
        return sequence.indexOf(this) + 1;
    }

    @Override
    public void interpretOn(Stack<SqueakNode> stack, Stack<SqueakNode> sequence) {
        stack.push(this);
    }

    @Override
    public void accept(PrettyPrintVisitor b) {
        b.visit(this);
    }

    @Override
    protected boolean isTaggedWith(Class<?> tag) {
        return ((tag == StandardTags.StatementTag.class) || (tag == StandardTags.CallTag.class)) && getSourceSection().isAvailable();
    }
}
