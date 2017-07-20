commit 317e32f07bc877b00f42ba38d14c66d704417bba
Author: mark_story <mark@mark-story.com>
Date:   Sun Oct 3 23:00:48 2010 -0400

    Making ShellDispatcher use exceptions instead of returning false and doing other goofy things.
    Adding MissingShellMethodException, MissingShellClassException and MissingShellFileException for use with ShellDispatcher.
    Removing duplicated tests, and refactoring them into separate tests with expected exceptions.