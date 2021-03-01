package com.devonfw.cobigen.cli.commands;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.JaccardDistance;
import org.apache.maven.shared.utils.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devonfw.cobigen.api.CobiGen;
import com.devonfw.cobigen.api.exception.InputReaderException;
import com.devonfw.cobigen.api.to.GenerableArtifact;
import com.devonfw.cobigen.api.to.GenerationReportTo;
import com.devonfw.cobigen.api.to.IncrementTo;
import com.devonfw.cobigen.api.to.TemplateTo;
import com.devonfw.cobigen.cli.CobiGenCLI;
import com.devonfw.cobigen.cli.artifacts.GenerableArtifactContainer;
import com.devonfw.cobigen.cli.artifacts.IncrementContainer;
import com.devonfw.cobigen.cli.artifacts.TemplateContainer;
import com.devonfw.cobigen.cli.constants.MessagesConstants;
import com.devonfw.cobigen.cli.input.InputFile;
import com.devonfw.cobigen.cli.input.OutputFile;
import com.devonfw.cobigen.cli.logger.CLILogger;
import com.devonfw.cobigen.cli.utils.CobiGenWrapper;
import com.devonfw.cobigen.cli.utils.ParsingUtils;
import com.devonfw.cobigen.cli.utils.ValidationUtils;
import com.google.googlejavaformat.java.FormatterException;

import ch.qos.logback.classic.Level;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * This class handles the generation command
 */
@Command(description = MessagesConstants.GENERATE_DESCRIPTION, name = "generate", aliases = {
"g" }, mixinStandardHelpOptions = true)
public class GenerateCommand implements Callable<Integer> {

  /**
   * Used for getting users input
   */
  private static final Scanner inputReader = new Scanner(System.in);

  /**
   * Selection threshold when user tries to find closest increments and templates
   */
  final double SELECTION_THRESHOLD = 0.1;

  /**
   * User input file
   */
  @Parameters(index = "0", arity = "1..*", split = ",", description = MessagesConstants.INPUT_FILE_DESCRIPTION)
  ArrayList<File> inputFiles = null;

  /**
   * User output project
   */
  @Option(names = { "--out", "-o" }, arity = "0..1", description = MessagesConstants.OUTPUT_ROOT_PATH_DESCRIPTION)
  File outputRootPath = null;

  /**
   * If this options is enabled, we will print also debug messages
   */
  @Option(names = { "--verbose", "-v" }, negatable = true, description = MessagesConstants.VERBOSE_OPTION_DESCRIPTION)
  boolean verbose;

  /**
   * This option provides the use of multiple available increments
   */
  @Option(names = { "--increments", "-i" }, split = ",", description = MessagesConstants.INCREMENTS_OPTION_DESCRIPTION)
  /**
   * Initialize increments variable
   */
  ArrayList<String> increments = null;

  /**
   * This option provide specified list of template
   */
  @Option(names = { "--templates", "-t" }, split = ",", description = MessagesConstants.TEMPLATES_OPTION_DESCRIPTION)
  /**
   * Initialize templates variable
   */
  ArrayList<String> templates = null;

  /**
   * Logger to output useful information to the user
   */
  private static Logger LOG = LoggerFactory.getLogger(CobiGenCLI.class);

  /**
   * Utils class for CobiGen related operations
   */
  private CobiGenWrapper cobigenWrapper = new CobiGenWrapper();

  private List<InputFile> inputFileList;

  /**
   * Constructor needed for Picocli
   */
  public GenerateCommand() {

    super();
  }

  /**
   * @return inputFileList
   */
  public List<InputFile> getInputFileList() {

    if (this.inputFileList == null) {
      List<InputFile> list = new ArrayList<>();
      CobiGen cg = this.cobigenWrapper.getCobiGen();
      for (File file : this.inputFiles) {
        list.add(new InputFile(file, cg));
      }
      this.inputFileList = list;
    }
    return this.inputFileList;
  }

  @Override
  public Integer call() throws Exception {

    if (this.verbose) {
      CLILogger.setLevel(Level.DEBUG);
    }
    OutputFile outputFile = null;
    if (this.outputRootPath != null) {
      outputFile = new OutputFile(this.outputRootPath);
    }

    List<InputFile> inputs = getInputFileList();
    LOG.debug("Input files and output root path confirmed to be valid.");

    GenerableArtifactContainer<?> container;
    List<String> artifactSelectors;
    if ((this.increments == null) && (this.templates != null)) {
      // User specified only templates, not increments
      container = new TemplateContainer();
      artifactSelectors = this.templates;
    } else {
      container = new IncrementContainer();
      artifactSelectors = this.increments;
    }
    preprocess(container);
    List<? extends GenerableArtifact> artifacts = selectArtifacts(artifactSelectors, container);
    if (artifacts.isEmpty()) {
      throw new IllegalStateException("There is no trigger matching the combination of all your input files.");
    }
    Path templateFolder = this.cobigenWrapper.getTemplatesFolder();
    for (InputFile inputFile : inputs) {
      generate(inputFile, outputFile, artifacts, templateFolder);
    }
    return 0;
  }

  /**
   * For each input file it is going to get its matching templates or increments and then performs an intersection
   * between all of them, so that the user gets only the templates or increments that will work
   *
   * @param cg CobiGen initialized instance
   */
  private void preprocess(GenerableArtifactContainer<?> container) {

    CobiGen cg = this.cobigenWrapper.getCobiGen();
    for (InputFile inputFile : getInputFileList()) {
      try {
        int matches = container.intersectByInput(cg, inputFile.getInput());
        if (matches == 0) {
          LOG.error(
              "Your input file '{}' is not valid as input for any generation purpose. It does not match any trigger.",
              inputFile.getFile().getName());
          if (inputFile.isJava()) {
            LOG.error("Check that your Java input file is following devon4j naming convention. "
                + "Explained on https://github.com/devonfw/devon4j/wiki/coding-conventions");
          } else if (inputFile.isYaml()) {
            LOG.error("Validate your OpenAPI specification, check that is following 3.0 standard. "
                + "More info here https://github.com/devonfw/cobigen/wiki/cobigen-openapiplugin#usage");
          }
          throw new IllegalArgumentException("Your input file is invalid");
        }
      } catch (InputReaderException e) {
        throw new IllegalStateException(
            "Invalid input for CobiGen, please check your input file '" + inputFile.getFile() + "'", e);
      }
    }
  }

  /**
   * Generates new templates or increments using the inputFile from the inputProject.
   *
   * @param inputFile {@link InputFile} the user wants to generate code from.
   * @param outputFile {@link OutputFile} (directory) the user wants to generate to.
   * @param artifacts the list of increments or templates that the user is going to use for generation.
   * @param templateFolder Path to load template utility classes from (root path of CobiGen templates).
   */
  public void generate(InputFile inputFile, OutputFile outputFile, List<? extends GenerableArtifact> artifacts,
      Path templateFolder) {

    CobiGen cg = this.cobigenWrapper.getCobiGen();
    ClassLoader classLoader = this.cobigenWrapper.getClassLoader();
    try {
      Object input = inputFile.getInput();

      if (outputFile == null) {
        // If user did not specify the output path of the generated files, we can use the current project folder
        outputFile = new OutputFile(ParsingUtils.getProjectRoot(inputFile.getFile()));
        LOG.info(
            "As you did not specify where the code will be generated, we will use the project of your current Input file.");
        LOG.debug("Generating to: {}", outputFile.getFile().getAbsolutePath());
      }
      LOG.info("Starting generation for input '{}', be patient as this can take a while...", inputFile);
      GenerationReportTo report = cg.generate(input, artifacts, outputFile.getPath(), false, classLoader,
          templateFolder);
      if (ValidationUtils.checkGenerationReport(report)) {
        Set<Path> generatedJavaFiles = report.getGeneratedFiles().stream()
            .filter(e -> FileUtils.getExtension(e.toAbsolutePath().toString()).equals("java"))
            .collect(Collectors.toSet());
        if (!generatedJavaFiles.isEmpty()) {
          try {
            ParsingUtils.formatJavaSources(generatedJavaFiles);
          } catch (FormatterException e) {
            LOG.info(
                "Generation was successful but we were not able to format your code. Maybe you will see strange formatting.",
                LOG.isDebugEnabled() ? e : null);
          }
        }
      }
    } catch (InputReaderException e) {
      LOG.error("Invalid input for CobiGen, please check your input file.", e);

    }
  }

  private List<? extends GenerableArtifact> selectArtifacts(List<String> artifactSelectors,
      GenerableArtifactContainer<?> container) {

    List<GenerableArtifact> userSelection = new ArrayList<>();
    if ((artifactSelectors == null) || artifactSelectors.isEmpty()) {
      // Print all matching generable artifacts add new arg userInputIncrements
      printFoundArtifacts(container, artifactSelectors);

      artifactSelectors = new ArrayList<>();
      for (String userArtifact : getUserInput().split(",")) {
        artifactSelectors.add(userArtifact);
      }
    }

    for (String selector : artifactSelectors) {
      if ("0".equals(selector) || "all".equalsIgnoreCase(selector)) {
        LOG.info("(0) All");
        return container.getList();
      }
      userSelection.add(container.getBySelector(selector));
    }
    return userSelection;
  }

  /**
   * Prints all the matching generable artifacts (increments or templates).
   *
   * @param container the {@link GenerableArtifactContainer}.
   * @param triggers the artifacts selected by the user as CLI arg.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void printFoundArtifacts(GenerableArtifactContainer<?> container, List<String> triggers) {

    String typeName = container.getTypeName();
    if (triggers != null) {
      LOG.info("Here are the {}s that may match your search.", typeName);
    }
    LOG.info("(0) " + "All");
    int i = 1;
    for (GenerableArtifact artifact : container.getList()) {
      String artifactDescription = ((GenerableArtifactContainer) container).getTitle(artifact);
      LOG.info("(" + i++ + ") " + artifactDescription);
    }
    LOG.info("Please enter the number(s) of {}(s) that you want to generate separated by comma.", typeName);
  }

  /**
   * Search for generable artifacts (increments or templates) matching the user input. Generable artifacts similar to
   * the given search string or containing it are returned.
   *
   * @param userInput the user's wished increment or template
   * @param matching all increments or templates that are valid to the input file(s)
   * @param c class type, specifies whether Templates or Increments should be preprocessed
   * @return Increments or templates matching the search string
   */
  @SuppressWarnings("unchecked")
  private ArrayList<? extends GenerableArtifact> search(String userInput, List<? extends GenerableArtifact> matching,
      Class<?> c) {

    Boolean isIncrements = c.getSimpleName().equals(IncrementTo.class.getSimpleName());
    Map<? super GenerableArtifact, Double> scores = new HashMap<>();

    for (int i = 0; i < matching.size(); i++) {
      if (!isIncrements) {
        String description = ((TemplateTo) matching.get(i)).getId();
        JaccardDistance distance = new JaccardDistance();
        scores.put(matching.get(i), distance.apply(description.toUpperCase(), userInput.toUpperCase()));
      } else {
        String description = ((IncrementTo) matching.get(i)).getDescription();
        String id = ((IncrementTo) matching.get(i)).getId();
        JaccardDistance distance = new JaccardDistance();
        Double descriptionDistance = distance.apply(description.toUpperCase(), userInput.toUpperCase());
        Double idDistance = distance.apply(id.toUpperCase(), userInput.toUpperCase());
        scores.put(matching.get(i), Math.min(idDistance, descriptionDistance));
      }
    }

    Map<? super GenerableArtifact, Double> sorted = scores.entrySet().stream().sorted(comparingByValue())
        .collect(toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));

    ArrayList<? super GenerableArtifact> chosen = new ArrayList<>();

    for (Object artifact : sorted.keySet()) {
      GenerableArtifact tmp;
      tmp = isIncrements ? (IncrementTo) artifact : (TemplateTo) artifact;
      if (!isIncrements) {
        String description = ((TemplateTo) artifact).getId();
        if (description.toUpperCase().contains(userInput.toUpperCase())
            || sorted.get(artifact) <= this.SELECTION_THRESHOLD) {
          chosen.add(tmp);
        }
      } else {
        String description = ((IncrementTo) artifact).getDescription();
        String id = ((IncrementTo) artifact).getId();
        if (description.equalsIgnoreCase(userInput) || id.equalsIgnoreCase(userInput)) {
          chosen.add(tmp);
          return (ArrayList<? extends GenerableArtifact>) chosen;
        }
        if ((description.toUpperCase().contains(userInput.toUpperCase())
            || id.toUpperCase().contains(userInput.toUpperCase()))
            || sorted.get(artifact) <= this.SELECTION_THRESHOLD) {
          chosen.add(tmp);
        }
      }
    }

    return (ArrayList<? extends GenerableArtifact>) chosen;
  }

  /**
   * Asks the user for input and returns the value
   *
   * @return String containing the user input
   */
  public static String getUserInput() {

    String userInput = "";
    userInput = inputReader.nextLine();
    return userInput;
  }

}
