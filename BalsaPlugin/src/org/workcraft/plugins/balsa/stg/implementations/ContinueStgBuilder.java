package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class ContinueStgBuilder extends ContinueStgBuilderBase {

	@Override
	public void buildStg(Continue component, ContinueStgInterface h,
			StrictPetriBuilder b) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public HandshakeComponentLayout getLayout(Continue properties, final ContinueHandshakes hs) {

		return new HandshakeComponentLayout() {
			
			@Override
			public Handshake getTop() {
				return null;
			}
			
			@Override
			public Handshake getBottom() {
				return null;
			}
			
			@Override
			public Handshake[][] getLeft() {
				return new Handshake[][]{
						{}
				};
			}
			
			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{
						{hs.inp}
				};
			}
		};
	}
}
