package org.workcraft.plugins.balsa.stg.generated;

public abstract class ActiveEagerFalseVariableStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<ActiveEagerFalseVariableStgBuilderBase.ActiveEagerFalseVariable, ActiveEagerFalseVariableStgBuilderBase.ActiveEagerFalseVariableHandshakes> {

    public static final class ActiveEagerFalseVariable {

        public ActiveEagerFalseVariable(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            readPortCount = (java.lang.Integer) parameters.get("readPortCount");
            specification = (java.lang.String) parameters.get("specification");
        }

        public final int width;

        public final int readPortCount;

        public final java.lang.String specification;
    }

    public static final class ActiveEagerFalseVariableHandshakes {

        public ActiveEagerFalseVariableHandshakes(ActiveEagerFalseVariable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            trigger = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "trigger", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            write = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "write", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            signal = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "signal", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            read = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "read", component.readPortCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync trigger;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg write;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync signal;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg> read;
    }

    @Override
    public final ActiveEagerFalseVariable makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new ActiveEagerFalseVariable(parameters);
    }

    @Override
    public final ActiveEagerFalseVariableHandshakes makeHandshakes(ActiveEagerFalseVariable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new ActiveEagerFalseVariableHandshakes(component, handshakes);
    }
}
