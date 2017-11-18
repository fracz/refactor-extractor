commit 1b8a986588a6566cd0c1c830a88d0baedfd15019
Author: Nadya.Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Thu Mar 28 17:14:55 2013 +0400

    tests for  HgMergeProvider merged

    *old tests for HgMergedProvider refactored and added to another test class (adapted for new test framework )
    *new HgTestUtil created and merged with old one
    *2 repositories created in base test class together
    *checking if statusBar is null before update widget added (because status bar widget may be null for example in testMode, but project not disposed)