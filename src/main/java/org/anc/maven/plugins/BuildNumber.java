package org.anc.maven.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Bump the build number for the project
 *
 * @goal build
 * @phase generate-resources
 */
public class BuildNumber extends AbstractMojo
{
	/**
	 * @parameter alias="file" default-value="src/main/resources/build"
	 * @required
	 */
	private File buildFile;

	public void execute() throws MojoExecutionException, MojoFailureException
	{
		int n = 1;
		if (buildFile.exists()) {
			try {
				Scanner scanner = new Scanner(buildFile);
				n = scanner.nextInt();
			}
			catch (FileNotFoundException | InputMismatchException e) {
				throw new MojoExecutionException("Unable to read the build file.", e);
			}
		}

		try (PrintWriter printer = new PrintWriter(new FileWriter(buildFile))) {
			printer.println(++n);
			getLog().info("Build Number: " + n);
		}
		catch (IOException e)
		{
			throw new MojoExecutionException("Unable to write the build file.", e);
		}
	}

}
