package de.hpi.swa.trufflesqueak.nodes.primitives.impl;

import com.oracle.truffle.api.dsl.Specialization;

import de.hpi.swa.trufflesqueak.model.CompiledMethodObject;
import de.hpi.swa.trufflesqueak.model.NativeObject;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveBinaryOperation;

public class PrimStringAt extends PrimitiveBinaryOperation {
    public PrimStringAt(CompiledMethodObject cm) {
        super(cm);
    }

    @Specialization
    char stringAt(NativeObject obj, int idx) {
        byte nativeAt0 = ((Long) obj.getNativeAt0(idx - 1)).byteValue();
        return (char) nativeAt0;
    }
}
