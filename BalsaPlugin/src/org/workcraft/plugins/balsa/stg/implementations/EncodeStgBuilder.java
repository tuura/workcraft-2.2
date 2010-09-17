package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class EncodeStgBuilder extends EncodeStgBuilderBase {

	@Override
	public void buildStg(Encode component, EncodeStgInterface h,
			StrictPetriBuilder b) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public HandshakeComponentLayout getLayout(Encode properties, final EncodeHandshakes hs) {

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
						hs.inp.toArray(new Handshake[0])
				};
			}
			
			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{
						{hs.out}
				};
			}
		};
	}
}
