commit 2441f529215d7be32a7c4a6b04a7b273867c9840
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Jul 31 23:04:44 2012 +0200

    Dependency report improvements. Refactoring, created a very simple api for accessing the graph.

    Introduced a higher level listener on top of the internal ResolvedConfigurationListener. It is a simplest possible api prototype for the dependency graph access. Just a first step - will change soon. Some rename jobs.