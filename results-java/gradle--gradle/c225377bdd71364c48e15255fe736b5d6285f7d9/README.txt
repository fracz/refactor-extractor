commit c225377bdd71364c48e15255fe736b5d6285f7d9
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Dec 21 18:17:14 2012 +0100

    Configuration on demand - reduced the impact, some refactoring in tests.

    Reduced the potential impact of evaluating the project when the project dependency is being resolved. Now it only happens when configuration-on-demand mode is 'on'. Some little improvements in the integ test (not yet fully refactored into a proper fixture code).