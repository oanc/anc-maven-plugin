package org.anc.maven.plugins;

import java.io.*;
import java.util.*;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal to write the Apache 2.0 license to a file.
 *
 * @goal license
 */
public class WriteLicense extends AbstractMojo
{
	public static final String DEFAULT_LICENSE = "LICENSE";
	
	/** File containing the license text. If omitted the text of the Apache
	 *  2.0 license will be loaded from the jar file.
	 * 
	 * @parameter expression="${license}" default-value="LICENSE";
	 */
	protected File license;
	
	/** Where the license text will be written.
	 * 
	 * @parameter expression="${destination}" default-value="${basedir}/LICENSE.txt"
	 */
	protected File destination;
	
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		List<String> text = loadLicense();
		writeLicense(text);
		log("License file created.");
	}

	protected void writeLicense(List<String> text) throws MojoExecutionException
	{
		PrintWriter out = null;
		try
		{
			out = new PrintWriter(new FileWriter(destination));
			for (String line : text)
			{
				out.println(line);
			}
		}
		catch (IOException ex)
		{
			log(ex);
			throw new MojoExecutionException(ex.getMessage());
		}
		finally
		{
			out.close();
		}
	}
	
	protected List<String> loadLicense() throws MojoExecutionException 
	{
		List<String> text = new LinkedList<String>();
		Reader reader = getReader();
		if (reader == null)
		{
			error("There was an error creating a Reader for the license text.");
			return text;
		}
		BufferedReader breader = new BufferedReader(reader);
		try
		{
			String line = breader.readLine();
			while (line != null)
			{
				text.add(line);
				line = breader.readLine();
			}
		}
		catch (IOException e)
		{
			log(e);
			throw new MojoExecutionException(e.getMessage());
		}
		return text;
	}

	/**
    * @return a Reader object used to read the license test, either from the
    * specified file, or if no file has been specified from the LICENSE 
    * resource included in the jar file. 
    */
   protected Reader getReader()
   {
	   Reader reader = null;
		if (license.exists())
		{
			try
         {
	         reader = new FileReader(license);
         }
         catch (FileNotFoundException e)
         {
	         // This means File.exists is broken...
         	return null;
         }
		}
		else
		{
			ClassLoader loader = this.getClass().getClassLoader();
			InputStream stream = loader.getResourceAsStream(DEFAULT_LICENSE);
			if (stream == null)
			{
				// The list is empty, which will tell the caller there was a 
				// problem.
				return null;
			}
			reader = new InputStreamReader(stream);
		}
	   return reader;
   }
	
	protected void log(String message)
	{
		this.getLog().info(message);
	}
	
	protected void error(String message)
	{
		this.getLog().error(message);
	}
	
	protected void log(Throwable e)
	{
		this.getLog().error(e);
	}
}
