commit 8aaea51a0a0d822e67404e3af6b970e8a323b877
Author: Ryan Ernst <ryan@iernst.net>
Date:   Thu May 25 12:18:45 2017 -0700

    Scripting: Move context definitions to instance type classes (#24883)

    This is a simple refactoring to move the context definitions into the
    type that they use. While we have multiple context names for the same
    class at the moment, this will eventually become one ScriptContext per
    instance type, so the pattern of a static member on the interface called
    CONTEXT can be used. This commit also moves the consolidated list of
    contexts provided by core ES into ScriptModule.