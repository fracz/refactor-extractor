commit b2a79cc1e860beb13560e8ed2ac326b580143f2c
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Thu Sep 13 19:12:45 2012 +0100

    MDL-35370 cloze qtype: distinguish wrong & unanswered subqs

    This affects the subquestions that appear as an embedded text input box.

    There are three cases:
    1. Input for subq left blank
    2. Input for subq was wrong, and matched by a * wildcard.
    3. Input for subq was wrong, and did not match any answer.

    2. and 3. should look identical, apart from any feedback in case 2.

    1. is different. The state should be displayed as "Not answered" even
    though the mark for this part is still shown as 0.

    There are some new unit tests for these cases.

    Also, we slighly improve handling of , for decimal point in multianswer,
    although there are still issues.

    While working on this, I made some minor clean-ups in shortanswer and
    numerical qtypes.