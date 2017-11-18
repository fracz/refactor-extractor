package org.jetbrains.idea.svn.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.add.SvnKitAddClient;

/**
 * @author Konstantin Kolosovsky.
 */
public class SvnKitClientFactory extends ClientFactory {

  public SvnKitClientFactory(@NotNull SvnVcs vcs) {
    super(vcs);
  }

  @Override
  protected void setup() {
    addClient = new SvnKitAddClient();
  }
}