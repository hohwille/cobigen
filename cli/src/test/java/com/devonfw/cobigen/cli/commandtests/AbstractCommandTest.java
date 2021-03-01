package com.devonfw.cobigen.cli.commandtests;

import java.io.File;
import java.io.IOException;

import org.apache.maven.shared.utils.io.FileUtils;
import org.assertj.core.api.Assertions;

import com.devonfw.cobigen.cli.commands.CobiGenCommand;

import picocli.CommandLine;

/**
 * Tests the usage of the generate command. Warning: Java 9+ requires -Djdk.attach.allowAttachSelf=true to be present
 * among JVM startup arguments.
 */
public abstract class AbstractCommandTest extends Assertions {

  private static final String MAVEN_PROJECT = "maven.project";

  /** Test resources root path */
  private static final String srcTestResourcesPath = "src/test/resources";

  /** Test resources root path */
  private static final String srcTestDataPath = srcTestResourcesPath + "/testdata";

  /** Test resources root path */
  private static final String srcExpectedPath = srcTestDataPath + "/expected";

  /** Test resources root path */
  private static final String srcTestProjectPath = srcTestDataPath + "/projects/" + MAVEN_PROJECT;

  /** Test resources root path */
  protected static final String targetPath = "target";

  /** Test resources root path */
  protected static final String targetProjectsPath = targetPath + "/test-projects";

  /** Test resources root path */
  protected static final String targetMavenProjectPath = targetProjectsPath + "/" + MAVEN_PROJECT;

  /**
   * Commandline to pass arguments to
   */
  private final CommandLine commandLine = new CommandLine(new CobiGenCommand());

  /**
   * This method executes the CobiGen CLI with the given {@code args} and asserts that the exit code is zero.
   *
   * @param args the commandline arguments.
   */
  protected void execute(String... args) {

    int exitCode = this.commandLine.execute(args);
    assertThat(exitCode).isEqualTo(0);
  }

  private File prepateTestProject() {

    try {
      File targetProjectPath = new File(targetMavenProjectPath);
      if (!targetProjectPath.isDirectory()) {
        File testProjectPath = new File(srcTestProjectPath);
        FileUtils.copyDirectoryStructure(testProjectPath, targetProjectPath);
      }
      File targetFolder = new File(targetProjectPath, "target");
      if (!targetFolder.isDirectory()) {
        Process process = new ProcessBuilder("mvn", "install").directory(targetProjectPath).start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
          throw new IllegalStateException("Maven failed with exit code " + exitCode);
        }
      }
      return targetProjectPath;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to prepare maven project for test!", e);
    }
  }

  /**
   * @param name the folder name of the test-project.
   * @return the populated {@link TestMavenProject}.
   */
  protected TestMavenProject createProject(String name) {

    try {
      File sourcePath = prepateTestProject();
      if (MAVEN_PROJECT.equals(name)) {
        throw new IllegalStateException("Test project name must not be " + name);
      }
      File destPath = new File(targetProjectsPath + "/" + name);
      if (destPath.exists()) {
        FileUtils.deleteDirectory(destPath);
      }
      FileUtils.copyDirectoryStructure(sourcePath, destPath);
      return new TestMavenProject(destPath);
    } catch (IOException e) {
      throw new IllegalStateException("Error preparing test project!", e);
    }
  }

  protected File getExpectedFile(String name) {

    if (name.endsWith(".java")) {
      return new File(srcExpectedPath + "/java/" + name);
    }
    throw new IllegalStateException("unknown file type " + name);
  }
}
