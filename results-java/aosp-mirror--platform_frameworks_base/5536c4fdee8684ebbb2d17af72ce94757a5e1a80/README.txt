commit 5536c4fdee8684ebbb2d17af72ce94757a5e1a80
Author: Roozbeh Pournader <roozbeh@google.com>
Date:   Wed Jun 1 16:49:27 2016 -0700

    Use bidi heuristics correctly in BoringLayout#isBoring()

    in order to determine if the text is RTL, the previous code ran
    chunks of the text (in code units) through the TextDirectionHeuristic
    instead of the whole text, which could result in misdetection as RTL
    in various cases, or missing some cases due to RTL characters getting
    split across chunks. Now we look at the whole text instead.

    This also refactors the code to make it clearer to understand and
    removes an unused signature for isBoring.

    Bug: 27702584
    Change-Id: I8d98614a0af28c0d4e61af5ab4a27a8a3ab8c9dc