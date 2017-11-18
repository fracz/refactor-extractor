commit 9148263d2c2544c70ebe8eb8a605ab3ed5f7b76d
Author: Craig Mautner <cmautner@google.com>
Date:   Tue Mar 20 17:24:00 2012 -0700

    Minor refactoring prior to major refactoring.

    Removal of blur layer.
    Deferral of Surface actions in BlackFrame from ctor to first use.
    Combine common test into single method okToDisplay().
    Remove redundant logic in DimAnimator.

    Change-Id: I43af0415794a8f142803ce94d7e17539aafac67d