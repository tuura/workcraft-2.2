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

package org.workcraft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

public class FileUtils{
	public static void copyFile(File in, File out)  throws IOException 
	{
		   FileOutputStream outStream = new FileOutputStream(out);
		   try
		   {
			   copyFileToStream(in, outStream);
		   }
		   finally
		   {
			   outStream.close();
		   }
	}
	
	public static void copyFileToStream(File in, OutputStream out)  throws IOException 
	{
	    FileChannel inChannel = new
	        FileInputStream(in).getChannel();
	    WritableByteChannel outChannel = Channels.newChannel(out);
	    try {
	        inChannel.transferTo(0, inChannel.size(),
	                outChannel);
	    } 
	    catch (IOException e) {
	        throw e;
	    }
	    finally {
	        if (inChannel != null) inChannel.close();
	        if (outChannel != null) outChannel.close();
	    }
	}

	public static File createTempDirectory(String prefix) 
	{
		File tempDir;
		try {
			tempDir = File.createTempFile(prefix, "");
		} catch (IOException e) {
			throw new RuntimeException("can't create a temp file");
		}
		tempDir.delete();
		if(!tempDir.mkdir())
			throw new RuntimeException("can't create a temp directory");
		return tempDir;
	}
	
	public static void deleteDirectoryTree(File dir) 
	{
		File [] files = dir.listFiles();
		if(files != null)
			for(File file : files)
				deleteDirectoryTree(file);
		
		dir.delete();
	}

	public static void writeAllText(File file, String source) throws IOException 
	{
		FileWriter writer = new FileWriter(file);
		writer.write(source);
		writer.close();
	}
}