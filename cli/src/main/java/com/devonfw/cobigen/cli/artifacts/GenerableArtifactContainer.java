package com.devonfw.cobigen.cli.artifacts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devonfw.cobigen.api.CobiGen;
import com.devonfw.cobigen.api.to.GenerableArtifact;

/**
 * Container for {@link GenerableArtifact}s avoiding duplicates and allowing {@link #getById(String) lookup} but
 * retaining order.
 *
 * @param <T> type of the {@link GenerableArtifact}s.
 * @since 2021.04.001
 */
public abstract class GenerableArtifactContainer<T extends GenerableArtifact> {

  private final Map<String, T> map;

  private final List<T> list;

  /**
   * The constructor.
   */
  public GenerableArtifactContainer() {

    super();
    this.map = new HashMap<>();
    this.list = new ArrayList<>();
  }

  /**
   * @param id the {@link GenerableArtifact#getId() ID} of the requested {@link GenerableArtifact}.
   * @return the requested {@link GenerableArtifact} or {@code null} if not found.
   */
  public T getById(String id) {

    return this.map.get(id);
  }

  /**
   * @param i the 1-based index of the {@link GenerableArtifact} in the {@link #getList() list}.
   * @return the requested {@link GenerableArtifact}.
   */
  public T getByIndex(int i) {

    int size = this.list.size();
    if (size == 0) {
      throw new IllegalStateException("No triggers available for generation!");
    } else if ((i < 1) || (i > size)) {
      throw new IllegalArgumentException("Invalid index " + i + " - has to be in the range from 1 to " + size);
    }
    return this.list.get(i - 1);
  }

  /**
   * @param selector the selector of the requested {@link GenerableArtifact}. Either {@link #getById(String) ID} or
   *        {@link #getByIndex(int) index}.
   * @return the requested {@link GenerableArtifact}.
   */
  public T getBySelector(String selector) {

    T artifact = getById(selector);
    if (artifact == null) {
      try {
        int index = Integer.parseInt(selector);
        return getByIndex(index);
      } catch (NumberFormatException e) {

      }
    }
    throw new IllegalStateException("Could not find " + getTypeName() + " for selector '" + selector + "'.");
  }

  /**
   * @return map
   */
  public Map<String, T> getMap() {

    return this.map;
  }

  /**
   * @return list
   */
  public List<T> getList() {

    return this.list;
  }

  /**
   * @param artifact the {@link GenerableArtifact} to check.
   * @return {@code true} if this container has an {@link GenerableArtifact} with the same
   *         {@link GenerableArtifact#getId() ID}.
   */
  public boolean contains(T artifact) {

    return this.map.containsKey(artifact.getId());
  }

  /**
   * @param artifact the {@link GenerableArtifact} to add.
   * @return {@code true} if the {@link GenerableArtifact} has been added, {@code false} otherwise (it has already been
   *         present).
   */
  public boolean add(T artifact) {

    T old = this.map.putIfAbsent(artifact.getId(), artifact);
    if (old == null) {
      this.list.add(artifact);
      return true;
    }
    return false;
  }

  /**
   * @param artifact the {@link GenerableArtifact} to remove.
   * @return {@code true} if the {@link GenerableArtifact} has been removed, {@code false} otherwise (it has not been
   *         present).
   */
  public boolean remove(T artifact) {

    T removed = this.map.remove(artifact.getId());
    if (removed == null) {
      return false;
    }
    assert (removed == artifact);
    this.list.remove(artifact);
    return true;
  }

  /**
   * @param artifacts the {@link Collection} of {@link GenerableArtifact}s to add.
   * @return the number of {@link GenerableArtifact}s that have actually been {@link #add(Collection) added} (no
   *         duplicates).
   */
  public int add(Collection<T> artifacts) {

    int count = 0;
    for (T artifact : artifacts) {
      boolean added = add(artifact);
      if (added) {
        count++;
      }
    }
    return count;
  }

  /**
   * @param other the {@link GenerableArtifactContainer} to build intersection with.
   */
  public void intersect(GenerableArtifactContainer<T> other) {

    for (T artifact : this.list) {
      if (!other.contains(artifact)) {
        remove(artifact);
      }
    }
  }

  /**
   * @param artifacts the {@link Collection} of {@link GenerableArtifact}s to build intersection with.
   */
  public void intersect(Collection<T> artifacts) {

    GenerableArtifactContainer<T> container = newInstance();
    container.add(artifacts);
    intersect(container);
  }

  /**
   * @param artifact the {@link GenerableArtifact}.
   * @return the title to display the given {@link GenerableArtifact}.
   */
  public String getTitle(T artifact) {

    return artifact.getId();
  }

  /**
   * @return the {@link Class} reflecting the contained {@link GenerableArtifact}s.
   */
  public abstract Class<T> getType();

  /**
   * @return the display name of the {@link GenerableArtifact}.
   */
  public String getTypeName() {

    return getType().getSimpleName();
  }

  /**
   * @return a new instance of this type of {@link GenerableArtifactContainer}.
   */
  public abstract GenerableArtifactContainer<T> newInstance();

  /**
   * @param cg the {@link CobiGen} instance.
   * @param input the input to find and add matching {@link GenerableArtifact}s for.
   * @return the total number of matching {@link GenerableArtifact}s.
   */
  public abstract int intersectByInput(CobiGen cg, Object input);

}
