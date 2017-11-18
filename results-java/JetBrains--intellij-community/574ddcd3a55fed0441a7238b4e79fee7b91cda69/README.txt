commit 574ddcd3a55fed0441a7238b4e79fee7b91cda69
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Tue Aug 23 19:27:43 2016 +0300

    PY-20138 Use existing indent of pasted fragment if caret is at first column

    Unless this indentation is going to break existing block structure,
    e.g. by splitting the containing function in the middle.
    Additionally I improved detection of an empty statement list when the
    caret is at the end of file.