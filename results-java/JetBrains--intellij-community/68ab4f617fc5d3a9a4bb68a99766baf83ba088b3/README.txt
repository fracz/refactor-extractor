commit 68ab4f617fc5d3a9a4bb68a99766baf83ba088b3
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Wed Oct 5 14:53:57 2011 +0400

    Apply patch refactoring.
    1) try to apply patch at given line (with all provided context)
    2) try to apply patch at some place (with all provided context)
    3) try to apply patch at some place. only lines that are going to be changed counts
    -- conflict cases --
    4) we parse base revision and try to apply patch to it and show 3-way merge
    5) base texts stored in patch are taken into account -> 3-way merge
    6) try to apply patch "approximately". show user a dialog with results and prompt

    so patch should be always applied _somehow_