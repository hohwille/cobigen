package com.devonfw.cobigen.cli.artifacts;

import java.util.List;

import com.devonfw.cobigen.api.CobiGen;
import com.devonfw.cobigen.api.to.TemplateTo;

/**
 * {@link GenerableArtifactContainer} for {@link TemplateTo templates}.
 */
public class TemplateContainer extends GenerableArtifactContainer<TemplateTo> {

  /**
   * The constructor.
   */
  public TemplateContainer() {

    super();
  }

  @Override
  public Class<TemplateTo> getType() {

    return TemplateTo.class;
  }

  @Override
  public String getTypeName() {

    return "template";
  }

  @Override
  public GenerableArtifactContainer<TemplateTo> newInstance() {

    return new TemplateContainer();
  }

  @Override
  public int intersectByInput(CobiGen cg, Object input) {

    List<TemplateTo> templates = cg.getMatchingTemplates(input);
    intersect(templates);
    return templates.size();
  }

}
