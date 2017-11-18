commit 2bca4067237323b4879253696058e237e1e4f398
Author: Dmitry Jemerov <yole@jetbrains.com>
Date:   Tue Sep 22 15:36:17 2015 +0200

    avoid holding a reference from UndoManager to the list of usages for a refactoring (KT-9214)