/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 * 
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.petrify;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.petrify.tasks.DrawAstgTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;

public class PSExporter implements Exporter {

	public PSExporter(Framework framework){
		this.framework = framework;
	}
	
	private Framework framework;

	public void export(Model model, OutputStream out) throws IOException,
	ModelValidationException, SerialisationException {

		if (model == null)
			throw new IllegalArgumentException("Model is null");
		
		File dotG = File.createTempFile("workcraft", ".g");
		
		final Result<? extends Object> result = framework.getTaskManager().execute(Export.createExportTask(model, dotG, Format.STG, framework.getPluginManager()), "Exporting to .g");
		
		if (result.getOutcome() != Outcome.FINISHED)
		{
			if (result.getOutcome() == Outcome.CANCELLED)
				return;
			else
				if (result.getCause() != null)
					throw new SerialisationException (result.getCause());
				else
					throw new SerialisationException ("Could not export model as .g");
		}
		
		File ps = File.createTempFile("workcraft", ".ps");
		
		DrawAstgTask task = new DrawAstgTask(dotG.getAbsolutePath(), ps.getAbsolutePath(), new ArrayList<String>());
		
		final Result<? extends ExternalProcessResult> draw_astgResult = framework.getTaskManager().execute(task, "Executing draw_astg");
		
		if (draw_astgResult.getOutcome() != Outcome.FINISHED) {
			if (draw_astgResult.getOutcome() == Outcome.CANCELLED)
				return;

			else
				if (draw_astgResult.getCause() != null)
					throw new SerialisationException (draw_astgResult.getCause());
				else
					throw new SerialisationException ("draw_astg failed with return code " + draw_astgResult.getReturnValue().getReturnCode() + "\n\n" +
							new String(draw_astgResult.getReturnValue().getErrors()) +"\n");
				
		}
		
		FileUtils.copyFileToStream(ps, out);
		
		dotG.delete();
		ps.delete();
	}

	public String getDescription() {
		return ".ps (Petrify draw_astg)";
	}

	public String getExtenstion() {
		return ".ps";
	}

	public int getCompatibility(Model model) {
		if (model instanceof STGModel)
			return Exporter.GENERAL_COMPATIBILITY;
		else
			return Exporter.NOT_COMPATIBLE;
	}

	@Override
	public UUID getTargetFormat() {
		return Format.PS;
	}
}