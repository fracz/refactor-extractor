commit d7b2f4f95f2be3db5928f31852caa324387d49a0
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Aug 16 10:36:16 2012 +0200

    More work in the improved dependency report.

    1. Improved coverage. Since we now build the new dependency graph model with every resolve the improved dependency report is also availabe when the configuration was already resolved.
    2. Commented out a unit tests that needs refactoring (making it readable and easy to maintain) & fixing.
    3. Added a workaround to the DependencyGraphBuilder to satisfy a questionable use case that is documented in a unit test. This needs to be revisited when developing the 'show unresolved dependencies' story. More info in the code comment.
    4. Fixed the NPE problem exposed by the integ test (it should be a part of the previous checkin, really).