package com.devonfw.cobigen.cli.artifacts;

import java.util.List;

import com.devonfw.cobigen.api.CobiGen;
import com.devonfw.cobigen.api.to.IncrementTo;

/**
 * {@link GenerableArtifactContainer} for {@link IncrementTo increments}.
 */
public class IncrementContainer extends GenerableArtifactContainer<IncrementTo> {

  /**
   * The constructor.
   */
  public IncrementContainer() {

    super();
  }

  @Override
  public Class<IncrementTo> getType() {

    return IncrementTo.class;
  }

  @Override
  public String getTypeName() {

    return "increment";
  }

  @Override
  public String getTitle(IncrementTo artifact) {

    return artifact.getId() + " (" + artifact.getDescription() + ")";
  }

  @Override
  public IncrementContainer newInstance() {

    return new IncrementContainer();
  }

  @Override
  public int intersectByInput(CobiGen cg, Object input) {

    List<IncrementTo> increments = cg.getMatchingIncrements(input);
    intersect(increments);
    return increments.size();
  }

}
