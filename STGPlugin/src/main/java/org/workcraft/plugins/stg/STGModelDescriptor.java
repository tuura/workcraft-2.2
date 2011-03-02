package org.workcraft.plugins.stg;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.interop.ServiceProviderImpl;

public class STGModelDescriptor implements ModelDescriptor
{
	@Override
	public String getDisplayName() {
		return "Signal Transition Graph";
	}

	@Override
	public MathModel createMathModel(StorageManager storage) {
		return new STG(storage);
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new STGVisualModelDescriptor();
	}

	@Override
	public ServiceProvider createServiceProvider(Model model) {
		return ServiceProviderImpl.createLegacyServiceProvider(model);
	}

	public static ServiceProvider getServices(STGModel model, HistoryPreservingStorageManager storage) {
		return ServiceProviderImpl.EMPTY.plusImplementation(STGModel.SERVICE_HANDLE, model).plusImplementation(HistoryPreservingStorageManager.SERVICE_HANDLE, storage);
	}
}
