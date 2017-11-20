commit 4400ba169fa46143990198f484099e0cecb6aa7b
Author: parrt <parrt@antlr.org>
Date:   Thu Dec 1 14:01:00 2011 -0800

    refactored RuleContext to move all noncritical fields down into ParserRuleContext. Widespread but minor changes. Using the more specific ParserRuleContext where appropriate.

    [git-p4: depot-paths = "//depot/code/antlr4/main/": change = 9509]