commit c51b2876ec5c0af449469a0f76bb38c51cfcff04
Author: Makoto Onuki <omakoto@google.com>
Date:   Wed May 4 15:24:50 2016 -0700

    Refactoring ShortcutManager + bug fixes.

    - Don't pass the ShortcutService instance as an argument.  This tiny
    optimization is no longer meaningful now that PackageShortcut and
    PackageLauncher have reference to ShortcutUser.

    - Rename mLauncherComponent to mDefaultLauncherComponent for clarity.

    - Don't instantiate ShortcutPackage instances when not needed.

    - Don't allow intents with a null action.

    - Also improve javadoc.

    Bug 28592642
    Bug 28474517
    Bug 28557169

    Change-Id: I8790d3494bf3b92c143c02824b0ed0e514504baa