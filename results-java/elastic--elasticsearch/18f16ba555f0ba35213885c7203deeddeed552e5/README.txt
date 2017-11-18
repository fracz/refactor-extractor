commit 18f16ba555f0ba35213885c7203deeddeed552e5
Author: Nik Everett <nik9000@gmail.com>
Date:   Fri Jun 2 11:02:44 2017 -0400

    Test: improve error message on leftover tasks

    After every REST test we wait for the list of pending cluster tasks
    to empty before moving on to the next task. If the list doesn't
    empty in 10 second we fail the test. This improves the error message
    when we fail the test to include the list of running tasks.