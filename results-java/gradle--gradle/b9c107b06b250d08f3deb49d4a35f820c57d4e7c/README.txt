commit b9c107b06b250d08f3deb49d4a35f820c57d4e7c
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Dec 8 17:39:05 2011 +0100

    Concurrent tooling api test runs in compatibility suite now...

    -Configured the min. tooling api version for the concurrency test to milestone-7. This is needed because there were changes in the consumer code that fixed some concurrency problems (e.g. changes in the connector services lifecycle - not using singletons for certain services). Min. target gradle version is also m7 because it does not make sense to target to previously released versions.
    -Improved the compatibility runner so that we can develop features that should be ran with 'current' tooling api or 'current' target gradle. It is very simple, I added new field to the annotation. It could be improved in a couple of ways but for now it may be ok.
    -Needed to add yet another package to the classloader used by the compatibility suite. I don't like it very much, I think I might just move some of our test code into the fixtures package instead.