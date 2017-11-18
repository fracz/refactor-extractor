package org.jetbrains.idea.svn.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.add.CmdAddClient;

/**
 * @author Konstantin Kolosovsky.
 */
public class CmdClientFactory extends ClientFactory {

  public CmdClientFactory(@NotNull SvnVcs vcs) {
    super(vcs);
  }

  @Override
  protected void setup() {
    addClient = new CmdAddClient();
  }
}