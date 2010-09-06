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

package org.workcraft.interop;
import java.io.FileFilter;

import org.workcraft.LegacyPlugin;
import org.workcraft.dom.Model;

public interface Converter extends LegacyPlugin, FileFilter {
	public Class <? extends Model> getSourceModelClass();
	public Class <? extends Model> getTargetModelClass();
	public boolean supportsVisual();
	public Model convert (Model source);
}