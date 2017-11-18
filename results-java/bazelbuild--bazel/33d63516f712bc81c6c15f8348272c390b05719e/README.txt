commit 33d63516f712bc81c6c15f8348272c390b05719e
Author: Googler <noreply@google.com>
Date:   Fri Dec 2 00:27:22 2016 +0000

    Prune modules when building modules themselves to reduce build times shorten
    critical paths.

    When the inputs of a module M are reduced to a set S, then that same set S also
    needs to be supplied to compile something that uses M. To do this, input
    discovery is divided into two stages. For each CppCompileAction, the first
    stage discovers the necessary modules (M above). These are then added as inputs
    to ensure that they are built. The second stage then finds all the modules (S
    above) that are required to use those and also adds them as inputs.

    For now, the new behavior is guarded by a new flag
    --experimental_prune_more_modules.

    This is currently implemented by reading the .d files of used modules add
    adding all their module dependencies. There are two noteworthy alternatives:
    1. Hack up input discovery to understand modules, e.g. if a modular header is
       hit, continue scanning from all it's headers. However, this seems very
       brittle and a lot of additional information would have to be passed to the
       input discovery.
    2. Directly pass the results from input discovery of one CppCompileAction to
       another one. However, this seems to tightly couple the execution of
       different CppCompileActions and might lead to a mess of different states,
       more memory consumption, etc.
    With the current implementation, there is a bit of runtime overhead of
    reading the .d files (many times). This could potentially be improved by
    caching the results. However, even without this caching, the runtime overhead
    is limited (<10%) for all builds I have tried (I have tried with builds where
    all the compile results are already in the executor's cache.

    --
    MOS_MIGRATED_REVID=140793217