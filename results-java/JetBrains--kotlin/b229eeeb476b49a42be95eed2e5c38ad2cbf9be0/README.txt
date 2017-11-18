commit b229eeeb476b49a42be95eed2e5c38ad2cbf9be0
Author: Alexey Sedunov <alexey.sedunov@jetbrains.com>
Date:   Thu Apr 13 15:31:40 2017 +0300

    Move: Prevent running refactoring helpers on partial move

    It may lead to some imports being wringly removed
    due to their references being unresolved yet

    Also fix NPE on package rename via directory move