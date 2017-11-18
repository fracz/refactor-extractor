commit c03ca7cc8bcef66f8e077c2451e8a9f68ee4733c
Author: tomlu <tomlu@google.com>
Date:   Mon Aug 21 19:26:41 2017 +0200

    Improve CustomCommandLine interface.

    In converting SpawnAction.Builder (multi-thousand line CL) users directly to CustomCommandLine I didn't like the resulting loss of readability, and the methods didn't feel very discoverable. Unless it's very convenient and readable to use CustomCommandLine, people will resort to non-memory efficient patterns by default. I'm holding that CL for this, which should offer a nicer interface.

    This CL removes VectorArg from the API contact surface area, instead creating 64 overloads for every valid combination of parameters. Pretty sad, but the methods dispatch straight to internal helper methods so it's mostly boilerplate to the tune of +400 LOC.

    Other changes:

    * Change ImmutableCollection -> Collection and copy the args directly into the internal args vector. Saves on collection object overhead and saves users from having to create immutable copies.
    * Change some names, notably add -> addAll for collection methods
    * Create additional missing overloads
    * Fix JavaDoc

    RELNOTES: None
    PiperOrigin-RevId: 165943879