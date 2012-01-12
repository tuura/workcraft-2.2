package org.workcraft.plugins.pcomp.tasks;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PcompResultHandler extends DummyProgressMonitor<ExternalProcessResult> {
	private final Framework framework;
	private final boolean showInEditor;

	public PcompResultHandler(Framework framework, boolean showInEditor) {
		this.framework = framework;
		this.showInEditor = showInEditor;
	}
	

	@Override
	public void finished(final Result<? extends ExternalProcessResult> result,
			String description) {
		try {
			SwingUtilities.invokeAndWait(new Runnable()
			{
			@Override
				public void run() {
				
			if (result.getOutcome() == Outcome.FAILED) {
				String message;
				if (result.getCause() != null) {
					message = result.getCause().getMessage();
					result.getCause().printStackTrace();
				}
				else
					message = "Pcomp errors: \n" + new String(result.getReturnValue().getErrors());
				JOptionPane.showMessageDialog(framework.getMainWindow(), message, "Parallel composition failed", JOptionPane.ERROR_MESSAGE);
			} else if (result.getOutcome() == Outcome.FINISHED) {
				try {
					File pcompResult = File.createTempFile("pcompresult", ".g");
					FileUtils.writeAllText(pcompResult, new String(result.getReturnValue().getOutput()));
					
					if (showInEditor) {
						WorkspaceEntry we = framework.getWorkspace().open(pcompResult, true);
						try {
							framework.getMainWindow().createEditorWindow(we);
						} catch (ServiceNotAvailableException e) {
							throw new NotImplementedException("Should not happen. TODO: make the type system take care of it.");
						}
					} else {
						framework.getWorkspace().add(pcompResult.getName(), pcompResult, true);
					}
				} catch (IOException e) {
					JOptionPane.showMessageDialog(framework.getMainWindow(), e.getMessage(), "Parallel composition failed", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				} catch (DeserialisationException e) {
					JOptionPane.showMessageDialog(framework.getMainWindow(), e.getMessage(), "Parallel composition failed", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
			
			}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
