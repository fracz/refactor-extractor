commit 720be6f2c3bdf5b075336e42643dbd6f4da5290e
Author: jamiesensei <jamiesensei>
Date:   Mon May 26 11:39:51 2008 +0000

    MDL-5241 "When manually grading, identity questions by their number within the quiz as well as by question name"

    Also added a drop down box to select question to mark instead of the viewquestions table. The drop down box is available at the top of every page, but only if there is more than one manually gradeable question in the quiz. If there is only one question it is automatically selected for marking.

    Also added a new constant QUESTION_EVENTS_GRADED during a general refactoring of UI code in grading report and some improvements to efficiency of SQL in report.

    Merged from Moodle 1.9 branch