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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal to echo the version number to a file.
 *
 * @goal version
 * @phase compile
 */
public class WriteVersion extends AbstractMojo implements org.apache.maven.plugin.Mojo
{
    /**
     * Location of the file.
     * @parameter alias="directory" expression="${basedir}" default-value="${basedir}"
     * @required
     */
    private File directory;

    /**
     * The version number to write
     * @parameter expression="${project.version}"
     * @required
     */
    private String version;

    /**
     * The file name to use. Default is VERSION
     * @parameter alias="filename" default-value="VERSION"
     * @required
     */
    private String filename;
    
    public void execute()
        throws MojoExecutionException
    {
   	 sanityCheck();
   	 if (!directory.exists())
   	 {
   		 directory.mkdirs();
   	 }
        File versionFile = new File( directory, filename );

        FileWriter w = null;
        try
        {
            w = new FileWriter( versionFile );

            w.write( version );
            getLog().info("Wrote version number : " + version + " to " 
            		+ versionFile.getPath());
        }
        catch ( IOException e )
        {
      	  getLog().error("Error creating version file " + versionFile, e);
            throw new MojoExecutionException( "Error creating file " + versionFile, e );
        }
        finally
        {
            if ( w != null )
            {
                try
                {
                    w.close();
                }
                catch ( IOException e )
                {
               	 getLog().error("Error closing the version file.", e);
                    // ignore
                }
            }
        }
    }
    
    protected void sanityCheck() throws MojoExecutionException
    {
   	 if (directory == null)
   	 {
   		 throw new MojoExecutionException("Directory parameter has not been set.");
   	 }
   	 if (version == null)
   	 {
   		 throw new MojoExecutionException("Version parameter has not been set.");   		 
   	 }
   	 if (filename == null)
   	 {
   		 throw new MojoExecutionException("Filename parameter has not been set.");
   	 }
    }
}
