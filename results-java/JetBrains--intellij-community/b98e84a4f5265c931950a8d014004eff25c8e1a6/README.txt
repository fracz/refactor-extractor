commit b98e84a4f5265c931950a8d014004eff25c8e1a6
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Mon Mar 5 13:30:27 2012 +0400

    Display VCS root errors in Settings | Version Control

    AbstractVcs#getRootChecker to return an instance of VcsRootChecker that would validate roots.
    Implement it in GitVcs: use GitRootErrorsFinder to validate roots.

    VcsDirectoryConfigurationPanel:
      - display invalid roots in red color
      - display unregistered roots below the table. Let add unregistered root right away.
      - refactor createMainComponent(): extract components creation to separate methods, use GridBagLayout.