commit b1944c26465423174a6aa81752b97a123e2139ae
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Mon Jan 27 15:36:40 2014 +0400

    IDEA-119506 active Mercurial bookmark shown instead of a named branch in status bar

    *HgStatusWidget refactored, because before the project opened HgRepositories were not initialized yet in appropriate RepositoryManager (seem to be race condition problem);
    *common getSelectedFile method moved to DvcsUtil with appropriate images dependency;
    *HgCurrentBranchStatusUpdater removed;
    *annotations added;
    *unnecessary fields and methods removed from HgVcs and appropriate deprecated tests