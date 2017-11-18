commit 77af3569363c859cbdb3bb3ba71fcd135a1d68a1
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Thu Jan 30 16:36:41 2014 +0400

    VcsRootDetector refactoring

    *VcsRootDetector Implementation moved to vcs-impl;
    *VcsRooDetector interface added;
    *below detector removed from appropriate class;
    *VcsRootDetectorInfo removed as unnecessary;
    *method for below project dir detection created in VcsIntegrationEnabler class