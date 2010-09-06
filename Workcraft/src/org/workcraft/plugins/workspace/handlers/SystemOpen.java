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

package org.workcraft.plugins.workspace.handlers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.workcraft.annotations.DisplayName;
import org.workcraft.workspace.FileHandler;

@DisplayName("Open using system default program")
public class SystemOpen implements FileHandler {

	public boolean accept(File f) {
		if (!f.getName().endsWith(".work"))
			return true;
		else
			return false;
	}


	public void execute(File f) {
		open(f);
	}


	public static void open(File f) {
		try {
			if (System.getProperty ("os.name").contains("Windows"))
				Runtime.getRuntime().exec (new String[] {"cmd", "/c", f.getAbsolutePath() });
			else
				Desktop.getDesktop().open(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
