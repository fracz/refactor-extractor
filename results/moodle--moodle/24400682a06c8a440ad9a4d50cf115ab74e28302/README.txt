commit 24400682a06c8a440ad9a4d50cf115ab74e28302
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Tue Jan 17 17:21:57 2012 +0000

    MDL-31065 question stats: fix analysis of responses not matching a given answer

    When shortanswer, numerical, calculated and calculatedsimple questions
    did not have a '*' match-anything answer, then any student response that
    did not match any of the teacher-given answers were classified as
    '[No response]', which was not right.

    This patch fixes that. Such responses are now classified as
    [Did not match any answer].

    While I was doing this, I noticed that the display of tolerance
    intervals for numerical questions in the response analysis was horrible,
    so I improved it.