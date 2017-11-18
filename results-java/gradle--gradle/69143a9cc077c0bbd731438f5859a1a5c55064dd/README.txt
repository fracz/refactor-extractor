commit 69143a9cc077c0bbd731438f5859a1a5c55064dd
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Aug 28 15:09:48 2012 +0200

    ResolutionResult API - refactoring.

    Stopped using recursive algorithm to build the graph. It should be much simpler now and easier to add new feature: 'from' on the DependencyResult.