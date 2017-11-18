commit 7c9e070c2b54feba39908fda99bbd73464c75b10
Author: Alexey Kudravtsev <cdr@intellij.com>
Date:   Wed Oct 26 17:54:32 2016 +0300

    refactored Condition<String> to Condition<CharSequence> to use it with cheaper vfile.getNameSequence()