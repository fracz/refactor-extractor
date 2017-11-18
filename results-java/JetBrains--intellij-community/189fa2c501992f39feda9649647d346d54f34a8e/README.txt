commit 189fa2c501992f39feda9649647d346d54f34a8e
Author: Yaroslav Lepenkin <yaroslav.lepenkin@jetbrains.com>
Date:   Mon Mar 30 17:01:09 2015 +0300

    refactoring, added tests, on line starting with closing parenthesis. Excluding braces, closed at line start from opened braces, when checking if line starts with continuation indent. Line starts with continuation indent if there is at least one opened parenthesis in the stack of opened ones.