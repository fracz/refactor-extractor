commit 40aecce165e207bdffe726950cfdcbe63f6acf77
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Jul 30 23:48:08 2014 +0200

    Decoupled most parts of conflict resolution from the dependency graph builder. This is needed to make place for improvements in conflict resolution (module replacements).

    1. Responsibilities pushed out:
     - the decision when conflict occurs
     - the selection of a version from candidates (it is still delegated to the respective resolver)
     - the choice of modules that are involved in the conflict resolution
    2. This refactoring does not change the existing behavior. It will be easier to grow the conflict-resolution features when decoupled from DependencyGraphBuilder
    3. The complexity was pushed to a separate package. Classes from that package don't use DependencyGraphBuilder internals. DependencyGraphBuilder talks to the conflict resolution via interfaces (mostly).