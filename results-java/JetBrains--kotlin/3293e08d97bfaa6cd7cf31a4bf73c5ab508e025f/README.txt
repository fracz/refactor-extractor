commit 3293e08d97bfaa6cd7cf31a4bf73c5ab508e025f
Author: Andrey Breslav <andrey.breslav@jetbrains.com>
Date:   Tue Feb 12 20:43:08 2013 +0400

    Compiler API refactored:
    * exec(messageCollector, arguments) introduced
    * exception-catching logic moved closer to the entry point
    * PrintingMessageCollector localized