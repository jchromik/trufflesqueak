package de.hpi.swa.trufflesqueak.nodes.primitives.impl;

import com.oracle.truffle.api.dsl.Specialization;
import de.hpi.swa.trufflesqueak.model.CompiledMethodObject;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveBinaryOperation;
import java.math.BigInteger;

public class PrimAdd extends PrimitiveBinaryOperation {
    public PrimAdd(CompiledMethodObject cm) {
        super(cm);
    }

    @Specialization(rewriteOn = ArithmeticException.class)
    int add(int a, int b) {
        return Math.addExact(a, b);
    }

    @Specialization
    long addOverflow(int a, int b) {
        return (long) a + (long) b;
    }

    @Specialization(rewriteOn = ArithmeticException.class)
    long add(long a, long b) {
        return Math.addExact(a, b);
    }

    @Specialization
    BigInteger add(BigInteger a, BigInteger b) {
        return a.add(b);
    }

    @Specialization
    double add(double a, double b) {
        return a + b;
    }
}
