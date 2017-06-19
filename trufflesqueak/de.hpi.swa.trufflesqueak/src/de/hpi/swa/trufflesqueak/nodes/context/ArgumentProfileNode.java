package de.hpi.swa.trufflesqueak.nodes.context;

import java.math.BigInteger;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.profiles.PrimitiveValueProfile;
import com.oracle.truffle.api.profiles.ValueProfile;

import de.hpi.swa.trufflesqueak.nodes.SqueakNode;

@NodeInfo(cost = NodeCost.NONE)
public class ArgumentProfileNode extends SqueakNode {
    @Child SqueakNode argumentNode;
    private final ConditionProfile objectProfile = ConditionProfile.createBinaryProfile();
    private final ValueProfile primitiveProfile = PrimitiveValueProfile.createEqualityProfile();
    private final ValueProfile identityProfile = ValueProfile.createIdentityProfile();
    private final ValueProfile classProfile = ValueProfile.createClassProfile();

    public ArgumentProfileNode(SqueakNode argNode) {
        argumentNode = argNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        final Object value = argumentNode.executeGeneric(frame);
        if (objectProfile.profile(value instanceof Long || value instanceof Integer || value instanceof Double || value instanceof Boolean || value instanceof BigInteger)) {
            return primitiveProfile.profile(value);
        } else {
            return classProfile.profile(identityProfile.profile(value));
        }
    }
}
