commit c56bc67cb6de12e011a3a953a05dcd00b775329f
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Thu Jun 13 20:40:10 2013 +0400

    branch popup refactoring for Git and Mercurial

    *common RootAction and BranchActionGroupPopup moved  to separate classes
    *static methods for getting repository names and relative paths moved from git specific class to DvcsUtil
    *action for creating new branch refactored as a separate base class for mercurial and git new branch classes