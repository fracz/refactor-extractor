commit 854ebba496126ea9473feeac9b9f2e0f794558c2
Author: everzet <ever.zet@gmail.com>
Date:   Wed Mar 5 08:59:13 2014 +0000

    HookExtension listens to events produced by dispatch able testers

    This commit decouples hooks from both testers and event dispatcher. Now
    hooks dispatching routine is handled through a simple event subscriber.
    This is a follow-up to the previous refactoring