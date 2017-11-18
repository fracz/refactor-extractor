commit 269403b032f965ff3847eb982c2f697229dc5a92
Author: Svetoslav <svetoslavganov@google.com>
Date:   Wed Aug 14 17:31:04 2013 -0700

    Implemented advanced printer selection and API refactoring.

    1. Added past printer history tracking and merging favorite printers
       with discovered printers.

    2. Added save as PDF support.

    3. Added all printers activity with search capability and optional
       add printers chooser (if any print service provides add printers
       activity)

    4. Refactored the printer discovery session APIs. Now one session
       can have multiple window discovery windows and the session stores
       the printers found during past discovery periods.

    5. Merged the print spooler and the print spooler service - much
       simpler and easier to maintain.

    Change-Id: I4830b0eb6367e1c748b768a5ea9ea11baf36cfad