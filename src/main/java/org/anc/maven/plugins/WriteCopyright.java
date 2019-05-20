/*-
 * Copyright 2009 The American National Corpus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.anc.maven.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/** Adds a copyright notice to the top of every *.java file in a source tree.
 * <p>
 * 
 * 
 * @goal copyright
 *
 * @author Keith Suderman
 *
 */
public class WriteCopyright extends AbstractMojo
{
   public static final String DEFAULT_COPYRIGHT_FILE = "COPYRIGHT";
   
   /** Root of the source code tree. 
    * <p>
    * This is the directory to be searched for Java source code files, that
    * is, files ending in <code>.java</code>. If {@link #replace} has been set
    * to <code>true</code> any existing copyright notice will be replaced. If
    * <code>replace</code> is false (the default) any existing copyright notice
    * is left intact.
    *
    * @parameter alias="source" expression="${src.dir}" default-value="${project.build.sourceDirectory}"
    */
   protected File srcDir;

   /** Plugin version.
    *  
    *  @parameter expression="${project.version}"
    *  @required
    */
   protected String version;
   
   /**
    * @parameter alias="create" expression="${create}" default-value="Boolean.FALSE"
    */
   protected Boolean writeFile;
   
   /**
    * Determines whether existing copyright notices will be replaced.
    * 
    * @parameter default-value="Boolean.TRUE" expression="${replace}"
    */
   protected Boolean replace;
   
   /**
    * 
    * @parameter alias="filename" expression="${create.file}" default-value="${project.build.directory}/COPYRIGHT"
    */
   protected File outputFile;
   
   
   /** Text file containing the copyright notice. 
    * 
    * @parameter expression="${notice.file}" default-value="COPYRIGHT"
    */
   protected File noticeFile;

   protected FileFilter filter = new JavaFilter();
   
   public void execute() throws MojoExecutionException
   {
   	log("Executing version " + version);
   	if (!srcDir.exists())
   	{
   		error("Source path not found.");
   		throw new MojoExecutionException("Source path not found : " 
   				+ srcDir.getPath());
   	}

   	log("Source path is : " + srcDir);
   	log("Notice file is : " + noticeFile);

      try
      {
         List<String> notice = loadNotice();
      	if (writeFile)
      	{
      		writeNotice(notice);
      	}
      	else
      	{
      		process(srcDir, notice);
      	}
      }
      catch (Exception e)
      {
         getLog().error(e);
         throw new MojoExecutionException(e.getMessage());
      }
   }

   protected void writeNotice(List<String> lines) throws MojoExecutionException
   {
   	try
      {
	      PrintWriter out = new PrintWriter(new FileWriter(outputFile));
	      for (String line : lines)
	      {
	      	out.println(line);
	      }
	      out.close();
	      log("Wrote copyright notice to " + outputFile.getPath());
      }
      catch (IOException e)
      {
	      throw new MojoExecutionException(e.getMessage());
      }
   }
   
   protected void log(String message)
   {
   	this.getLog().info(message);
   }
   
   protected void error(String message)
   {
   	this.getLog().error(message);
   }
   
   /** Loads the text of the copyright notice into a list of strings. */
   protected List<String> loadNotice() throws IOException
   {
      List<String> result = new LinkedList<String>();
//      BufferedReader reader = new BufferedReader(new FileReader(noticeFile));
      BufferedReader reader = new BufferedReader(getReader(noticeFile));
      String line = reader.readLine();
      Calendar c = Calendar.getInstance();
      int year = c.get(Calendar.YEAR);
      while (line != null)
      {
         line = line.replaceAll("%YEAR%", Integer.toString(year));
         result.add(line);
         line = reader.readLine();
      }
      return result;
   }

   /**
    * Returns a java.io.Reader used to load the copyright noticed to be attached
    * to each file.  If the file <code>file</code> exists the copyright notice will
    * be read from the file. If <code>file</code> does not exist the reader
    * returned will read the COPYRIGHT resource included in the jar file.
    * 
    * @throws FileNotFoundException iff <code>File.exists</code> is broken.
    */
   protected Reader getReader(File file) throws FileNotFoundException
   {
   	if (file.exists())
   	{
   		return new FileReader(file);
   	}
   	// else
   	InputStream stream = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_COPYRIGHT_FILE);
   	if (stream != null)
   	{
   		return new InputStreamReader(stream);
   	}
   	return null;
   }
   
   protected void process(File file, List<String> notice) throws IOException
   {
      if (file.isFile())
      {
         addNotice(file, notice);
      }
      else
      {
         File[] contents = file.listFiles(filter);
         for (File f : contents)
         {
            process(f, notice);
         }
      }
   }
   
   /**
    * Loads the Java source code and writes it back to the same file with
    * the text of the copyright notice prepended.
    */
   protected void addNotice(File file, List<String> notice) throws IOException
   {
      List<String> code = loadJava(file);
      if (code == null)
      {
         log(file.getPath() + " already contains a copyright notice.");
         return;
      }
      
      log("Adding copyright notice to " + file.getPath());
      PrintWriter writer = new PrintWriter(file);
      for (String s : notice)
      {
         writer.println(s);
      }
//      writer.println();
      for (String statement : code)
      {
         writer.println(statement);
      }
      writer.close();
   }

   /**
    * Loads the Java source code into a list of strings.  All text before
    * the <tt>package</tt> statement is discarded.  Each line in the source,
    * including blank lines, occupies one item in the list.
    */
   protected List<String> loadJava(File java) throws IOException
   {
      List<String> source = new LinkedList<String>();
      BufferedReader reader = new BufferedReader(new FileReader(java));
      
      // Skip everything until we reach the package declaration
      String line = reader.readLine();
      while (line != null && !line.startsWith("package"))
      {
//         if (line.contains("Copyright") && !replace)
//         {
//            line = null;
//            break;
//         }
         line = reader.readLine();
      }
      
      if (line == null)
      {
         // We either found a copyright statement or reached the end of the 
         // file without finding a package statement. In either case the 
         // caller should do nothing.
         reader.close();
//         throw new IOException("Unable to locate the package statement in " + java.getPath());
         return null;
      }
      
      while (line != null)
      {
         source.add(line);
         line = reader.readLine();
      }
      reader.close();
      return source;
   }
   
}

class JavaFilter implements FileFilter
{
	public boolean accept(File file)
	{
		if (file.isDirectory())
		{
			return true;
		}
      String filename = file.getName();
		return filename.endsWith(".java") || filename.endsWith(".groovy");
	}
}
