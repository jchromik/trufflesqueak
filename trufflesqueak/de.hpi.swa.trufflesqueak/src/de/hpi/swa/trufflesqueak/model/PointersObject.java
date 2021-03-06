package de.hpi.swa.trufflesqueak.model;

import com.oracle.truffle.api.interop.TruffleObject;

import de.hpi.swa.trufflesqueak.SqueakImageContext;

public class PointersObject extends AbstractPointersObject implements TruffleObject {
    public PointersObject(SqueakImageContext img) {
        super(img);
    }

    public PointersObject(SqueakImageContext img, ClassObject sqClass, Object[] objects) {
        super(img, sqClass, objects);
    }

    public PointersObject(SqueakImageContext image, ClassObject classObject, int size) {
        super(image, classObject, size);
    }

    @Override
    public BaseSqueakObject shallowCopy() {
        return new PointersObject(image, getSqClass(), getPointers().clone());
    }
}
