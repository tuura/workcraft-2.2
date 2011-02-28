package org.workcraft.plugins.desij;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.desij.tasks.DesiJResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.workspace.Workspace;

public class DecompositionResultHandler extends DummyProgressMonitor<DesiJResult> {
	
	private Framework framework;
	private boolean logFileOutput;
	private final Path<String> specificationPath;
	
	public DecompositionResultHandler(Framework framework, Path<String> specificationPath, boolean logFileOutput) {
		this.framework = framework;
		this.specificationPath = specificationPath;
		this.logFileOutput = logFileOutput;
	}

	@Override
	public void finished(Result<? extends DesiJResult> result, String description) {
		
		if (result.getOutcome() == Outcome.FINISHED) {
			final Workspace workspace = framework.getWorkspace();
			
			DesiJResult desijResult = result.getReturnValue();
			
			// logfile output at console
			if (this.logFileOutput)
				try {
					BufferedReader br = new BufferedReader(new FileReader(desijResult.getLogFile()));
					String currentLine;
					while ((currentLine = br.readLine()) != null) {
						System.out.println(currentLine);
					}
				} catch (FileNotFoundException e1) {
					throw new RuntimeException(e1);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			
			// components output in workspace
			if (desijResult.getComponentFiles() != null)  // independent from the compiler 
				if (desijResult.getComponentFiles().length > 0) {
					
					Path<String> componentsDirectoryPath = Path.append(specificationPath.getParent(), specificationPath.getNode() + "-components"); 
					
					try {
						workspace.delete(componentsDirectoryPath);
					} catch (OperationCancelledException e) {
						return;
					}
					
					File componentsDir = workspace.getFile(componentsDirectoryPath);
					componentsDir.mkdirs();
					
					for (File file : desijResult.getComponentFiles()) {
						File target = new File(componentsDir, getComponentSuffix(file) + ".g");
						file.renameTo(target);
					}
					
					// output of the petrify- or mpsat-equations
					if (desijResult.getEquationFile() != null) { // synthesis successful
						try {
							BufferedReader br = new BufferedReader(new FileReader(desijResult.getEquationFile()));
							String currentLine;
							while ((currentLine = br.readLine()) != null) {
								System.out.println(currentLine);
							}
						} catch (FileNotFoundException e1) {
							throw new RuntimeException(e1);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					
					workspace.fireWorkspaceChanged();
					
					/*// pop up MessageBox
					final String successMessage = "Decomposition succeeded.";
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(null, successMessage);
						}
					});*/
				}
			if (desijResult.getModifiedSpecResult() != null) {
				Path<String> resultPath = Path.append(specificationPath.getParent(), specificationPath.getNode()+"_modifiedResult.g");
				
				try {
					workspace.delete(resultPath);
				} catch (OperationCancelledException e) {
					return;
				}
														
				File modifiedSpecification = workspace.getFile(resultPath);
				desijResult.getModifiedSpecResult().renameTo(modifiedSpecification);
				
				workspace.fireWorkspaceChanged(); // update of workspace window
				
			/*	// pop up MessageBox
				final String successMessage = "DesiJ operation succeeded.";
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null, successMessage);
					}
				});*/
			}
		}
		else if (result.getOutcome() != Outcome.CANCELLED) {
			String errorMessage = "DesiJ execution failed :-(";
			
			if (result.getCause() != null)
				errorMessage += "\n\n" + result.getCause().getMessage();
			
			final String err = errorMessage;
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, err, "Error", JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}

	private String getComponentSuffix(File componentFile) {

		String fileName = componentFile.getName(); // stg.g__final_suffix.g

		// determine the suffix
		String suffix = fileName.substring(
				fileName.lastIndexOf("__final_") + 8,
				fileName.lastIndexOf(".g"));
		
		return suffix;
	}

}
