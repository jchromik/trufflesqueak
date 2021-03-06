package de.hpi.swa.trufflesqueak.model;

import de.hpi.swa.trufflesqueak.SqueakImageContext;
import de.hpi.swa.trufflesqueak.util.Chunk;

public abstract class SqueakObject extends BaseSqueakObject {
    private int hash;
    private ClassObject sqClass;

    public SqueakObject(SqueakImageContext img) {
        super(img);
        hash = 0;
    }

    public SqueakObject(SqueakImageContext img, ClassObject klass) {
        super(img);
        hash = 0;
        sqClass = klass;
    }

    @Override
    public void fillin(Chunk chunk) {
        hash = chunk.getHash();
        sqClass = chunk.getSqClass();
    }

    @Override
    public ClassObject getSqClass() {
        return sqClass;
    }

    @Override
    public void setSqClass(ClassObject newCls) {
        sqClass = newCls;
    }

    @Override
    public boolean become(BaseSqueakObject other) {
        if (other instanceof SqueakObject) {
            int hash2 = ((SqueakObject) other).hash;
            ((SqueakObject) other).hash = this.hash;
            this.hash = hash2;

            ClassObject sqClass2 = ((SqueakObject) other).sqClass;
            ((SqueakObject) other).sqClass = this.sqClass;
            this.sqClass = sqClass2;
            return true;
        }
        return false;
    }

    @Override
    public int squeakHash() {
        if (hash == 0) {
            hash = super.squeakHash();
        }
        return hash;
    }
}
