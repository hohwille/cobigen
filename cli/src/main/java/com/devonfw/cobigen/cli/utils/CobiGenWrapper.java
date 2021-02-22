package com.devonfw.cobigen.cli.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.devonfw.cobigen.api.CobiGen;
import com.devonfw.cobigen.impl.CobiGenFactory;

/**
 * Wrapper to find templates and create {@link CobiGen} instance.
 */
public class CobiGenWrapper {

  private final CobiGen cobiGen;

  private final Path templatesFolder;

  private final ClassLoader classLoader;

  /**
   * The constructor.
   */
  public CobiGenWrapper() {

    super();
    try {
      this.classLoader = Thread.currentThread().getContextClassLoader();
      URL resource = this.classLoader.getResource("src/main/templates/context.xml");
      if (resource == null) {
        throw new IllegalStateException("CobiGen-Templates have not been found!");
      } else {
        String url = resource.toString().replace("src/main/templates/context.xml", "");
        URI uri = URI.create(url);
        this.cobiGen = CobiGenFactory.create(uri);
        this.templatesFolder = Paths.get(uri);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * @return the {@link CobiGen} instance.
   */
  public CobiGen getCobiGen() {

    return this.cobiGen;
  }

  /**
   * @return the {@link Path} pointing to the folder containing the templates.
   */
  public Path getTemplatesFolder() {

    return this.templatesFolder;
  }

  /**
   * @return the {@link ClassLoader} to use.
   */
  public ClassLoader getClassLoader() {

    return this.classLoader;
  }

}
