commit eda67299cf14de315af12cbf8d5024e02fa5b1aa
Author: Craig Mautner <cmautner@google.com>
Date:   Sun Apr 28 13:50:14 2013 -0700

    Generic refactoring for clarity.

    - Log.* was being interspersed with Slog.*.
    - WindowState Rects were being converted to local variables making
    it harder to find all references to them.

    Change-Id: I868a32028604d46dbbc15b005a440f0571336293