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

import java.io.*;
import java.util.Stack;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Writes the version number to a Java class file so it can be access
 * programm\atically from within the software.
 * 
 * @author Keith Suderman
 * @goal version-class
 * @phase generate-sources
 */

public class WriteVersionClass extends AbstractMojo
{
   /**
    * The package name to use for the generated class
    * 
    * @parameter alias="package" expression="${package.name}" default-value=${groupId}
    * @required
    */   
   private String packageName;
   
   /** 
    * The name to use for the generated class file, without the .java
    * extension
    * 
    * @parameter alias="class" expression="${class.name}" default-value="Version"
    * @required
    */
   private String className;
   
   /**
    * The version number to be written to the class file.
    * 
    * @parameter expression="${version}" default-value="${project.version}"
    * @required
    */
   private String version;
   
   /**
    * The source directory for the project
    * 
    * @parameter default-value="${project.build.sourceDirectory}"
    * @required
    */
   private String sourceDirectory;
   
   /**
    * The string used to declare the version field.  We declare this here since
    * we search for this string when reading a .java file looking for a previous
    * version number.
    */
   protected static final String VARIABLE_DECL = "private static final String version";
   
   protected IndentationLevel indent = new IndentationLevel();
   protected PrintWriter out = null;
   
   public void execute() throws MojoExecutionException, MojoFailureException
   {
      if (packageName == null)
      {
         throw new MojoFailureException("The packageName has not been specified.");
      }
      if (className == null)
      {
         throw new MojoFailureException("The class name has not been specified.");
      }
      if (sourceDirectory == null)
      {
         throw new MojoFailureException("The source directory has not been specified.");
      }
      
      File packageFile = new File(sourceDirectory + "/" + packageName.replaceAll("\\.", "/"));
      if (!packageFile.exists())
      {
         if (!packageFile.mkdirs())
         {
            throw new MojoFailureException("Unable to create the package directory " 
                  + packageFile.getPath());
         }
      }
   
//      File resourceDirectory = new File(resourceDirName);
//      if (!resourceDirectory.exists())
//      {
//         if (!resourceDirectory.mkdirs())
//         {
//            throw new MojoFailureException("Unable to create the resource directory "
//                  + resourceDirectory.getPath());
//         }
//      }
      
      File classFile = new File(packageFile, className + ".java");
      if (checkVersionExists(classFile, version))
      {
         getLog().info("Class for the current version already exists, skipping.");
      }
      else
      {
         writeJava(classFile);
         getLog().info("Generated " + classFile.getPath());
      }
      
//      File propertiesFile = new File(resourceDirectory, resourceFile);
//      writeProperties(propertiesFile);
//      getLog().info("Generated properties file " + propertiesFile.getPath());
   }
   
   protected void writeJava(File javaFile) throws MojoExecutionException
   {
      try
      {
         out = new PrintWriter(new FileWriter(javaFile));
         code("package " + packageName + ";");
         line();
         code("/* DO NO EDIT. This file is geneated automatically by Maven. */");
         code("/**");
         code(" * Class used to determine the current version number of the application.");
         code(" */");
         code("public final class " + className);
         openBrace();
         code(VARIABLE_DECL + " = \"" + version + "\";");         
         line();
         code("public static String getVersion() { return version; }");
         closeBrace();
      }
      catch (IOException e)
      {
         throw new MojoExecutionException(e.getMessage());
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }
      }
   }
   
   protected boolean checkVersionExists(File javaFile, String current)
   {
      if (!javaFile.exists())
      {
         getLog().info("No Version.java file found.");
         return false;
      }
      
      BufferedReader reader = null;
      try
      {
         reader = new BufferedReader(new FileReader(javaFile));
         String line = null;
         while ((line = reader.readLine()) != null)
         {
            if (line.indexOf(VARIABLE_DECL) >= 0)
            {
               int open = line.indexOf('"');
               if (open > 0)
               {
                  ++open;
                  int close = line.indexOf('"', open + 1);
                  if (close > open)
                  {
                     String previous = line.substring(open, close);
                     return previous.equals(current);
                  }
               }
            }
         }
      }
      catch (IOException e)
      {
         getLog().error(e);
         return false;
      }
      finally
      {
         if (reader != null)  try 
         {
            reader.close();
         }
         catch (Exception e)
         {
            
         }
      }
      return false;
   }
   
   protected void _old_writeJava(File classFile) throws MojoExecutionException
   {
      
//      InputStream in = this.getClass().getClassLoader().getResourceAsStream(TEMPLATE);
      
//      PrintWriter out = null;
      try
      {
         out = new PrintWriter(new FileWriter(classFile));
         code("package " + packageName + ";");
         line();
         code("import java.io.InputStream;");
//         code("import java.io.IOException;");
         code("import java.util.Properties;");
         code("import org.anc.util.VersionBase;");
         line();
         code("/* DO NO EDIT. This file is geneated automatically. */");
         code("/**");
         code(" * Class used to determine the current version number of the applications.");
         code(" * During the build Maven injects a version.properties files into the jar");
         code(" * that contains the version number specified in the POM file. The properties");
         code(" * file is read from the jar and the version number (string) is cached for");
         code(" * later use.");
         code(" */");
         code("public final class " + className + " extends VersionBase");
         openBrace();
         code("protected static String cache = null;");
         line();
         code("/**");
         code(" * Returns the version number of the application as specified in the");
         code(" * Maven POM file.");
         code(" */");
         code("public static String getVersion()");
         openBrace();
         code("if (cache != null)");
         openBrace();
         code("return cache;");
         closeBrace();
         code("InputStream in = getInputStream();");
         code("if (in == null)");
         openBrace();
         code("cache = \"X\";");
         code("return cache;");
         closeBrace();
         line();
         code("Properties props = new Properties();");
         code("try");
         openBrace();
         code("props.load(in);");
         code("cache = (String) props.get(\"version\");");
         closeBrace();
         code("catch (Exception e)");
         openBrace();
         code("cache = \"X\";");
         closeBrace();
         code("return cache;");
         closeBrace();
         closeBrace();
      }
      catch (IOException e)
      {
         throw new MojoExecutionException(e.getMessage());
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }
      }
   }
   
   protected void writeProperties(File propertiesFile) throws MojoExecutionException
   {
//      File propertiesFile = new File("src/main/resources/version.properties");
      try
      {
         out = new PrintWriter(new FileWriter(propertiesFile));
         out.println("version=${project.version}");
      }
      catch (IOException e)
      {
         throw new MojoExecutionException(e.getMessage());
      }
      finally
      {
         if (out != null)
         {
            out.close();
            out = null;
         }
   }
   
   }
   protected void openBrace()
   {
      code("{");
      indent.more();
   }
   
   protected void closeBrace()
   {
      indent.less();
      code("}");
   }
   
   protected void code(String line)
   {
      out.println(indent + line);
   }
   
   protected void line()
   {
      out.println();
   }
}

class IndentationLevel
{
   protected String currentIndent = "";
   protected Stack<String> stack = new Stack<String>();
   
   public void more()
   {
      stack.push(currentIndent);
      currentIndent += "\t";
   }
      
   public void less()
   {
      if (stack.isEmpty())
      {
         currentIndent = "";
         return;
      }
      currentIndent = stack.pop();
   }
   
   @Override
   public String toString() { return currentIndent; }
}