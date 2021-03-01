package com.devonfw.cobigen.cli.input;

import java.io.File;

import com.devonfw.cobigen.api.CobiGen;
import com.devonfw.cobigen.cli.utils.CobiGenUtils;

/**
 * Wrapper for a {@link File} acting as input for the generation.
 */
public class InputFile extends CliFile {

  private final Object input;

  /**
   * The constructor.
   *
   * @param file the {@link #getFile() file to wrap}.
   * @param cg the {@link CobiGen} instance.
   */
  public InputFile(File file, CobiGen cg) {

    super(file);
    this.input = CobiGenUtils.getValidCobiGenInput(cg, this.file, isJava());
  }

  @Override
  protected String getType() {

    return "input file";
  }

  @Override
  protected File normalizeAndVerify(File rawFile) {

    File normalizedFile = super.normalizeAndVerify(rawFile);
    if (normalizedFile.isDirectory()) {
      throw new IllegalArgumentException(
          "Your " + getType() + " '" + rawFile + "' is a directory. CobiGen cannot understand that. Please use files.");
    }
    return normalizedFile;
  }

  /**
   * @return the result of {@link CobiGen} input reader parsing the input {@link #getFile() file}.
   */
  public Object getInput() {

    return this.input;
  }

}
