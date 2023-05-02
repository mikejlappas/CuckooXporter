/*****************************************************************************
 * Name : Mike
 * Date : 21 Aug 2022
 * File : Exporter.java
 *****************************************************************************/

import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileOptions;
import ghidra.app.decompiler.DecompileResults;
import ghidra.app.util.headless.HeadlessScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;
import ghidra.program.model.listing.Program;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Exporter extends HeadlessScript
{
	@Override
	public void run()
	{
		if (!isRunningHeadless())
		{
			printerr("This script must be run in headless mode !");
			return;
		}

		String[] strArgumentsArray = getScriptArgs();
		if (strArgumentsArray.length == 0)
		{
			printerr("No output filepath was provided !");
			return;
		}

		String strOutputFilepath = strArgumentsArray[0];
		File file = new File(strOutputFilepath);
		try
		{
			if (file.exists())
			{
				printerr("The file or directory '" + strOutputFilepath + "' already exists !");
				return;
			}
		}
		catch (SecurityException securityException)
		{
			printerr("Failed to access the file or directory '" + strOutputFilepath + "', permission denied !");
			return;
		}

		Program program = this.getCurrentProgram();
		DecompInterface decompInterface = new DecompInterface();
		if (!decompInterface.openProgram(program))
		{
			printerr("Failed to initialize a decompiler process !");
			return;
		}

		DecompileOptions decompileOptions = new DecompileOptions();
		decompileOptions.setWARNCommentIncluded(false);
		if (!decompInterface.setOptions(decompileOptions))
		{
			printerr("Failed to set the decompiler options !");
			decompInterface.closeProgram();
			return;
		}

		FileWriter fileWriter;
		try
		{
			fileWriter = new FileWriter(file);
		}
		catch (IOException ioException)
		{
			printerr("Failed to construct a 'FileWriter' object !");
			decompInterface.closeProgram();
			return;
		}

		FunctionIterator functionIterator = program.getListing().getFunctions(true);
		while (functionIterator.hasNext())
		{
			Function function = functionIterator.next();
			String strFunctionName = function.getName();
			if (!strFunctionName.startsWith("FUN_"))
				continue;

			DecompileResults decompileResults = decompInterface.decompileFunction(function, 30, null);
			if (!decompileResults.decompileCompleted())
			{
				printerr("Failed to decompile the function '" + strFunctionName + "', skipping !");
				continue;
			}

			try
			{
				fileWriter.write(decompileResults.getDecompiledFunction().getC());
			}
			catch (IOException ioException)
			{
				printerr("Failed to write the decompiled version of the function '" + strFunctionName + "' " +
					 "to the file '" + strOutputFilepath + "', skipping !");
				continue;
			}

			printf("Successfully saved the decompiled version of the function '%s' to the file '%s' !\n", strFunctionName, strOutputFilepath);
		}

		decompInterface.closeProgram();

		try
		{
			fileWriter.close();
		}
		catch (IOException ioException)
		{
			printerr("Failed to close the 'FileWriter' object !");
		}
	}
}
