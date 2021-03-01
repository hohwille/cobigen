package com.devonfw.cobigen.cli.commandtests;

import java.io.File;
import java.io.IOException;

import org.apache.maven.shared.utils.io.FileUtils;

/**
 * A maven project for testing CobiGen CLI. Will be created
 */
public class TestMavenProject {

  private final File basedir;

  private File core;

  private File coreSrcMainJava;

  private File entity;

  private File coreDataAccessApi;

  /**
   * The constructor.
   *
   * @param basedir the {@link #getBasedir() project base directory}.
   */
  public TestMavenProject(File basedir) {

    super();
    this.basedir = basedir;
    assert (basedir.isDirectory());
  }

  /**
   * @return the base directory of this {@link TestMavenProject}.
   */
  public File getBasedir() {

    return this.basedir;
  }

  /**
   * @return the {@link File} pointing to the core module of this {@link TestMavenProject}.
   */
  public File getCore() {

    if (this.core == null) {
      this.core = new File(this.basedir, "core");
    }
    return this.core;
  }

  /**
   * @return the {@link File} pointing to src/main/java in {@link #getCore() core}.
   */
  public File getCoreSrcMainJava() {

    if (this.coreSrcMainJava == null) {
      this.coreSrcMainJava = new File(getCore(), "src/main/java");
    }
    return this.coreSrcMainJava;
  }

  private File getCoreDataAccessApi() {

    if (this.coreDataAccessApi == null) {
      this.coreDataAccessApi = new File(getCoreSrcMainJava(), "com/maven/project/sampledatamanagement/dataaccess/api");
    }
    return this.coreDataAccessApi;
  }

  /**
   * @return the {@link File} pointing to the SampleDataEntity Java source-code file.
   */
  public File getEntity() {

    if (this.entity == null) {
      this.entity = new File(getCoreDataAccessApi(), "SampleDataEntity.java");
    }
    return this.entity;
  }

  /**
   * @return the {@link File} pointing to the generated SampleDataRepository Java source-code file.
   */
  public File getRepository() {

    if (this.entity == null) {
      this.entity = new File(getCoreDataAccessApi(), "repo/SampleDataRepository.java");
    }
    return this.entity;
  }

  /**
   * Deletes this {@link TestMavenProject}.
   */
  public void delete() {

    try {
      FileUtils.deleteDirectory(this.basedir);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to delete test project!", e);
    }
  }

}
