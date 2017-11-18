commit 2288ca33c3ed8cddeb778c1eaf78f8b089c17a68
Author: peter <peter@jetbrains.com>
Date:   Thu Feb 16 17:10:22 2017 +0100

    perform refactoring from preview in a transaction to prevent anyone from starting dumb mode during progresses (EA-93720 - INRE: FileBasedIndexImpl.handleDumbMode)