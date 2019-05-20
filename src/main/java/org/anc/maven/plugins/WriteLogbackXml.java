package org.anc.maven.plugins;

import java.io.*;
import java.util.*;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Writes the default logback XML files to src/main/resources and 
 * src/test/resources.
 * 
 * @goal logback
 * 
 * @author Keith Suderman
 *
 */
public class WriteLogbackXml extends AbstractMojo
{

   public void execute() throws MojoExecutionException, MojoFailureException
   {
      InputStream is = this.getClass().getClassLoader().getResourceAsStream("template.xml");
      if (is == null)
      {
         throw new MojoFailureException("Unable to find the default logback.xml file.");
      }
      
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      List<String> lines = new LinkedList<String>();
      String line;
      try
      {
         line = reader.readLine();
         while (line != null)
         {
            lines.add(line);
            line = reader.readLine();
         }
      }
      catch (IOException e)
      {
         throw new MojoExecutionException("Unable to load logback.xml", e);
      }
      finally
      {
         try
         {
            reader.close();
         }
         catch (IOException e)
         {
         }
      }
      
      write(lines, "src/main/resources", "logback.xml");
      write(lines, "src/test/resources", "logback-test.xml");
   }

   private void write(List<String> lines, String path, String name) throws MojoFailureException, MojoExecutionException
   {
      File dir = new File(path);
      if (!dir.exists())
      {
         if (!dir.mkdirs())
         {
            throw new MojoFailureException("Unable to create " + path);
         }
      }
      File outfile = new File(dir, name);
      PrintWriter out = null;
      try
      {
         this.getLog().info("Writing " + outfile.getPath());
         out = new PrintWriter(new FileWriter(outfile));
         for (String line : lines)
         {
            out.println(line);
         }
      }
      catch (Exception e)
      {
         throw new MojoExecutionException("Error writing " + name, e);
      }
      finally
      {
         out.flush();
         out.close();
      }
   }
}
