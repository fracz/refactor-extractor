commit aef03d9f2d459e7160c33b8eb6c8ebe125d7ee55
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Sun Nov 3 18:47:33 2013 +0400

    [log] "Checkout Revision" action to the new log, some refactoring

    * Instead of making VcsLog a project service and acquiring an instance
      from the ServiceManager, provide it from the DataProvider via the
      defined DataKey.
    * Make the MainFrame the main Log component and the DataProvider.
    * Implement GitCheckoutRevisionAction (just delegate to the GitBrancher)
    * Introduce VcsLog#getSelectedDetails instead of getSelectedCommits +
      getDetailsIfAvailable, because it can be better for performance
      reasons: getting the Node by Hash in the GraphTableModel can be
      expensive.