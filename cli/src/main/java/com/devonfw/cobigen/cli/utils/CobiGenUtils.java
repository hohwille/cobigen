package com.devonfw.cobigen.cli.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.sf.mmm.code.impl.java.JavaContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devonfw.cobigen.api.CobiGen;
import com.devonfw.cobigen.api.InputInterpreter;
import com.devonfw.cobigen.api.exception.InputReaderException;
import com.devonfw.cobigen.api.to.IncrementTo;
import com.devonfw.cobigen.api.to.TemplateTo;
import com.devonfw.cobigen.cli.CobiGenCLI;
import com.devonfw.cobigen.cli.constants.MavenConstants;
import com.google.common.base.Charsets;

/**
 * Utilities class for CobiGen related operations. For instance, it creates a new CobiGen instance and
 * registers all the plug-ins
 */
public class CobiGenUtils {

    /**
     * Logger instance for the CLI
     */
    private static Logger LOG = LoggerFactory.getLogger(CobiGenCLI.class);

    /**
     * Extracts an artificial POM which defines all the CobiGen plug-ins that are needed
     * @param rootCLIPath
     *            path where the artificial POM will be extracted to
     * @return the extracted POM file
     */
    public File extractArtificialPom(Path rootCLIPath) {
        File pomFile = rootCLIPath.resolve(MavenConstants.POM).toFile();
        if (!pomFile.exists()) {
            try (InputStream resourcesIS = (getClass().getResourceAsStream("/" + MavenConstants.POM));) {
                Files.copy(resourcesIS, pomFile.getAbsoluteFile().toPath());
            } catch (IOException e1) {
                LOG.error(
                    "Failed to extract CobiGen plugins pom into your computer. Maybe you need to use admin permissions.");
            }
        }
        return pomFile;
    }

    /**
     * For Increments Returns a list that retains only the elements in this list that are contained in the
     * specified collection (optional operation). In other words, the resultant list removes from this list
     * all of its elements that are not contained in the specified collection.
     *
     * @param currentList
     *            list containing elements to be retained in this list
     * @param listToIntersect
     *            second list to be used for the intersection
     * @return resultant list containing increments that are in both lists
     */
    public static List<IncrementTo> retainAllIncrements(List<IncrementTo> currentList,
        List<IncrementTo> listToIntersect) {

        List<IncrementTo> resultantList = new ArrayList<>();

        for (IncrementTo currentIncrement : currentList) {
            String currentIncrementDesc = currentIncrement.getDescription().trim().toLowerCase();
            for (IncrementTo intersectIncrement : listToIntersect) {

                String intersectIncrementDesc = intersectIncrement.getDescription().trim().toLowerCase();

                if (currentIncrementDesc.equals(intersectIncrementDesc)) {
                    resultantList.add(currentIncrement);
                    break;
                }
            }
        }
        return resultantList;
    }

    /**
     * For Templates Returns a list that retains only the elements in this list that are contained in the
     * specified collection (optional operation). In other words, the resultant list removes from this list
     * all of its elements that are not contained in the specified collection.
     *
     * @param currentList
     *            list containing elements to be retained in this list
     * @param listToIntersect
     *            second list to be used for the intersection
     * @return resultant list containing increments that are in both lists
     */
    public static List<TemplateTo> retainAllTemplates(List<TemplateTo> currentList, List<TemplateTo> listToIntersect) {

        List<TemplateTo> resultantList = new ArrayList<>();

        for (TemplateTo currentTemplate : currentList) {
            String currentTemplateDesc = currentTemplate.getId().trim().toLowerCase();
            for (TemplateTo intersectTemplate : listToIntersect) {

                String intersectTemplateDesc = intersectTemplate.getId().trim().toLowerCase();

                if (currentTemplateDesc.equals(intersectTemplateDesc)) {
                    resultantList.add(currentTemplate);
                    break;
                }
            }
        }
        return resultantList;
    }

    /**
     * Processes the given input file to be converted into a valid CobiGen input. Also if the input is Java,
     * will create the needed class loader
     * @param cg
     *            CobiGen instance
     * @param inputFile
     *            user's input file
     * @param isJavaInput
     *            true if input is Java code
     * @return valid cobiGen input
     * @throws InputReaderException
     *             throws {@link InputReaderException} when the input file could not be converted to a valid
     *             CobiGen input
     */
    public static Object getValidCobiGenInput(CobiGen cg, File inputFile, boolean isJavaInput)
        throws InputReaderException {
        Object input;
        // If it is a Java file, we need the class loader
        if (isJavaInput) {
            JavaContext context = ParsingUtils.getJavaContext(inputFile, ParsingUtils.getProjectRoot(inputFile));
            input = process(cg, inputFile, context.getClassLoader());
        } else {
            input = process(cg, inputFile, null);
        }
        return input;
    }

    /**
     * Processes the given file to be converted into any CobiGen valid input format
     * @param file
     *            {@link File} converted into any CobiGen valid input format
     * @param cl
     *            {@link ClassLoader} to be used, when considering Java-related inputs
     * @param inputInterpreter
     *            parse cobiGen compliant input from the file
     * @throws InputReaderException
     *             if the input retrieval did not result in a valid CobiGen input
     * @return a CobiGen valid input
     */
    public static Object process(InputInterpreter inputInterpreter, File file, ClassLoader cl)
        throws InputReaderException {
        if (!file.exists() || !file.canRead()) {
            throw new InputReaderException("Could not read input file " + file.getAbsolutePath());
        }
        Object input = null;
        try {
            input = inputInterpreter.read(Paths.get(file.toURI()), Charsets.UTF_8, cl);
        } catch (InputReaderException e) {
            LOG.debug("No input reader was able to read file {}", file.toURI(), e);
        }
        if (input != null) {
            return input;
        }
        throw new InputReaderException("The file " + file.getAbsolutePath() + " is not a valid input for CobiGen.");
    }

}
