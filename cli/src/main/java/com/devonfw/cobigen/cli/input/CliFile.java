package com.devonfw.cobigen.cli.input;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

import org.apache.maven.shared.utils.io.FileUtils;

/**
 * Wrapper for a {@link File} for the generation.
 */
public abstract class CliFile {

  private static final File USER_DIR = new File(System.getProperty("user.dir"));

  /** @see #getFile() */
  protected final File file;

  /** @see #getExtension() */
  protected final String extension;

  /**
   * The constructor.
   *
   * @param file the {@link #getFile() file to wrap}.
   */
  public CliFile(File file) {

    super();
    this.file = normalizeAndVerify(file);
    this.extension = FileUtils.getExtension(this.file.getName()).toLowerCase(Locale.US);
  }

  /** @return the type of this file. */
  protected abstract String getType();

  /**
   * @param rawFile the {@link File} to normalize and verify.
   * @return the normalized {@link File}.
   */
  protected File normalizeAndVerify(File rawFile) {

    File normalizedFile = normalize(rawFile);
    if (!normalizedFile.exists()) {
      throw new IllegalArgumentException("Your " + getType() + " '" + rawFile + "' could not be found.");
    } else if (!normalizedFile.canRead()) {
      throw new IllegalArgumentException(
          "Your input file '" + rawFile + "' cannot be read. Please check file permissions on the file");
    }
    return normalizedFile;
  }

  /**
   * @param file the {@link File} to normalize.
   * @return the normalized {@link File} unquoted .
   */
  public static File normalize(File file) {

    String path = file.getPath();
    int length = path.length();
    if (length > 2) {
      char first = path.charAt(0);
      char last = path.charAt(length - 1);
      if ((first == last) && ((first == '"') || (first == '\''))) {
        path = path.substring(1, length - 1);
        file = new File(path);
      }
    }
    if (!file.exists()) {
      File relocated = new File(USER_DIR, file.toString());
      if (relocated.exists()) {
        file = relocated;
      }
    }
    return file;
  }

  /**
   * @return the underlying {@link File}.
   */
  public File getFile() {

    return this.file;
  }

  /**
   * @return the {@link #getFile() file} as {@link Path}.
   */
  public Path getPath() {

    return this.file.getAbsoluteFile().toPath();
  }

  /**
   * @return the file extension in lower-case excluding the dot.
   */
  public String getExtension() {

    return this.extension;
  }

  /**
   * @return {@code true} if the {@link #getFile() file} has "java" {@link #getExtension() extension}.
   */
  public boolean isJava() {

    return "java".equals(this.extension);
  }

  /**
   * @return {@code true} if the {@link #getFile() file} has "yml" or "yaml" {@link #getExtension() extension}.
   */
  public boolean isYaml() {

    return "yml".equals(this.extension) || "yaml".equals(this.extension);
  }

  @Override
  public String toString() {

    return this.file.toString();
  }

}
