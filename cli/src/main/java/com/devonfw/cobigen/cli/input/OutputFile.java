package com.devonfw.cobigen.cli.input;

import java.io.File;

/**
 * Wrapper for a {@link File} acting as output of the generation.
 */
public class OutputFile extends CliFile {

  /**
   * The constructor.
   *
   * @param file the {@link #getFile() file to wrap}.
   */
  public OutputFile(File file) {

    super(file);
  }

  @Override
  protected String getType() {

    return "output file";
  }

  @Override
  protected File normalizeAndVerify(File rawFile) {

    File normalizedFile = super.normalizeAndVerify(rawFile);
    if (!normalizedFile.isDirectory()) {
      throw new IllegalArgumentException("Your " + getType() + " '" + rawFile + "' is not a directory.");
    }
    return normalizedFile;
  }

}
