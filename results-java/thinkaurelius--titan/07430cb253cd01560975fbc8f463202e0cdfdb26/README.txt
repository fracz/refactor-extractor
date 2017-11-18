commit 07430cb253cd01560975fbc8f463202e0cdfdb26
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Fri Jun 6 02:36:16 2014 -0400

    Attempting to consolidate Gremlin shells

    Separating Faunus from the ScriptEngine implementation turned out to
    be non-trivial because Faunus needs a ScriptEngine implementation in
    several places: not just for gremlin.sh, but also in ScriptMap and
    ScriptInputFormat.  I still don't see a satisfactory way to separate
    these concerns short of a major titan-hadoop refactoring, so for now
    I'm compromising and just adopting the Faunus ScriptEngine,
    ResultHookClosure, ScriptExecutor, etc.

    This commit also deletes the shaded titan03 dependencies from
    titan-hadoop and reenables maven-enforcer-plugin in the same.  The
    shaded jar needs some attention/testing before it makes a return.
    Reenabling enforcer in titan-hadoop exposed a couple of unmanaged
    dependency versions that are now controlled in the top-level pom.