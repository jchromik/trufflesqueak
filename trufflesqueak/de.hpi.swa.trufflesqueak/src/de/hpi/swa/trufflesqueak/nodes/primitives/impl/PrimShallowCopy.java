package de.hpi.swa.trufflesqueak.nodes.primitives.impl;

import com.oracle.truffle.api.dsl.Specialization;

import de.hpi.swa.trufflesqueak.model.BaseSqueakObject;
import de.hpi.swa.trufflesqueak.model.CompiledMethodObject;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveUnaryOperation;

public class PrimShallowCopy extends PrimitiveUnaryOperation {
    public PrimShallowCopy(CompiledMethodObject cm) {
        super(cm);
    }

    @Specialization
    Object copy(BaseSqueakObject self) {
        return self.shallowCopy();
    }
}
