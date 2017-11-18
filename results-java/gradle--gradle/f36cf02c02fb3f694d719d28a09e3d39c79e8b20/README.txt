commit f36cf02c02fb3f694d719d28a09e3d39c79e8b20
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Jun 25 01:25:34 2011 +0200

    Refactoring after linked resources were added to the tooling api eclipse model. Working towards tooling API compatibility suite. Details:

    - Added a proper test that verifies compatibility with previous version of Gradle. This is still work in progress! We're aiming to have a rich suite (possibly most tests) that will be ran against the prior versions of Gradle.
    - The compatibility test exposed an issue that needed to be fixed. The cleanest way to fix it was removing redundant VersionX interfaces.
    - Work in progress in ToolingApiSpecification - refactorings towards the tooling api compatibility suite. Added somewhat naive support for running previous gradle version
    - Moved 2 tests that expose bugs fixed after M3 was released. Those tests are moved to specific package and they cannot be a part of compatibility suite because the bugs were present in previous versions of Gradle.
    - I needed to remove @Rule ability from ToolingApi class. BTW. I don't understand why was it a Rule in first place and not just a plain object.