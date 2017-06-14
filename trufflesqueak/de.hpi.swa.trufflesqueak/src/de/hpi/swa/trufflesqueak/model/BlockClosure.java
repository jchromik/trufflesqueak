package de.hpi.swa.trufflesqueak.model;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstanceVisitor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameUtil;
import de.hpi.swa.trufflesqueak.SqueakImageContext;
import de.hpi.swa.trufflesqueak.exceptions.PrimitiveFailed;
import de.hpi.swa.trufflesqueak.util.Chunk;

public class BlockClosure extends BaseSqueakObject {
    private static final int BLKCLSR_OUTER_CONTEXT = 0;
    private static final int BLKCLSR_COMPILEDBLOCK = 1;
    private static final int BLKCLSR_NUMARGS = 2;
    private static final int BLKCLSR_RECEIVER = 3;
    private static final int BLKCLSR_SIZE = 4;
    @CompilationFinal private Object receiver;
    @CompilationFinal(dimensions = 1) private Object[] stack;
    @CompilationFinal private Object frameMarker;
    @CompilationFinal private Object context;
    @CompilationFinal private CompiledBlockObject block;

    public BlockClosure(SqueakImageContext img) {
        super(img);
    }

    public BlockClosure(Object frameId, CompiledBlockObject compiledBlock, Object rcvr, Object[] copied) {
        super(compiledBlock.image);
        block = compiledBlock;
        frameMarker = frameId;
        receiver = rcvr;
        stack = copied;
    }

    private BlockClosure(BlockClosure original) {
        this(original.frameMarker, original.block, original.receiver, original.stack);
        context = original.context;
    }

    @Override
    public void fillin(Chunk chunk) {
        // FIXME
    }

    private Object getOrPrepareContext() {
        if (context == null) {
            return Truffle.getRuntime().iterateFrames(new FrameInstanceVisitor<Object>() {
                @Override
                public Object visitFrame(FrameInstance frameInstance) {
                    Frame frame = frameInstance.getFrame(FrameInstance.FrameAccess.READ_ONLY);
                    FrameDescriptor frameDescriptor = frame.getFrameDescriptor();
                    FrameSlot markerSlot = frameDescriptor.findFrameSlot(CompiledCodeObject.MARKER);
                    Object marker = FrameUtil.getObjectSafe(frame, markerSlot);
                    if (marker == frame) {
                        context = ContextObject.createReadOnlyContextObject(image, frame);
                        return context;
                    }
                    return null;
                }
            });
        }
        return context;
    }

    @Override
    public Object at0(int i) {
        switch (i) {
            case BLKCLSR_OUTER_CONTEXT:
                return getOrPrepareContext();
            case BLKCLSR_COMPILEDBLOCK:
                return block;
            case BLKCLSR_NUMARGS:
                return block.getNumArgs();
            case BLKCLSR_RECEIVER:
                return receiver;
            default:// FIXME
                return stack[i - BLKCLSR_SIZE];
        }
    }

    @Override
    public void atput0(int i, Object obj) {
        switch (i) {
            case BLKCLSR_OUTER_CONTEXT:
                context = obj;
                break;
            case BLKCLSR_COMPILEDBLOCK:
                block = (CompiledBlockObject) obj;
                break;
            case BLKCLSR_NUMARGS:
                throw new PrimitiveFailed();
            case BLKCLSR_RECEIVER:
                receiver = obj;
                break;
            default:
                stack[i - BLKCLSR_SIZE] = obj;
        }
    }

    @Override
    public boolean become(BaseSqueakObject other) {
        if (other instanceof BlockClosure && super.become(other)) {
            Object[] stack2 = stack;
            stack = ((BlockClosure) other).stack;
            ((BlockClosure) other).stack = stack2;
            return true;
        }
        return false;
    }

    @Override
    public ClassObject getSqClass() {
        return image.blockClosureClass;
    }

    @Override
    public int size() {
        return instsize() + varsize();
    }

    @Override
    public int instsize() {
        return BLKCLSR_SIZE;
    }

    @Override
    public int varsize() {
        return stack.length;
    }

    public Object[] getStack() {
        return stack;
    }

    public Object getReceiver() {
        return receiver;
    }

    public RootCallTarget getCallTarget() {
        return block.getCallTarget();
    }

    public Assumption getCallTargetStable() {
        return block.getCallTargetStable();
    }

    public CompiledBlockObject getCompiledBlock() {
        return block;
    }

    public Object[] getFrameArguments(Object... objects) {
        if (block.getNumArgs() != objects.length) {
            throw new PrimitiveFailed();
        }
        Object[] arguments = new Object[1 /* receiver */ +
                        objects.length +
                        stack.length +
                        1 /* this */];
        arguments[0] = getReceiver();
        for (int i = 0; i < objects.length; i++) {
            arguments[1 + i] = objects[i];
        }
        for (int i = 0; i < stack.length; i++) {
            arguments[1 + objects.length + i] = stack[i];
        }
        arguments[arguments.length - 1] = this;
        return arguments;
    }

    public Object getFrameMarker() {
        return frameMarker;
    }

    @Override
    public BaseSqueakObject shallowCopy() {
        return new BlockClosure(this);
    }
}
