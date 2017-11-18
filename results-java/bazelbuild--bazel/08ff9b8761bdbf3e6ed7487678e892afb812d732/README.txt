commit 08ff9b8761bdbf3e6ed7487678e892afb812d732
Author: ulfjack <ulfjack@google.com>
Date:   Thu Sep 28 04:08:06 2017 -0400

    Make the state in RuleContext explicit

    This isn't ideal - RuleContext should not have state, but this ended up
    happening between adding a cache and refactoring how make variables are
    discovered.

    I have carefully traced back all callers that provide custom make variable
    suppliers and added an init call to their rule initialization. Note that the
    ConfigurationMakeVariableContext is _cached_, so callers that call in without
    any make variable suppliers and then call again with them would get the context
    from the previous call.

    We now enforce that the ConfigurationMakeVariableContext is only initialized
    once, and that this happens before any usage, which is slightly better than the
    previous state, where initialization was silently ignored on any subsequent
    call.

    Progress on #2475.

    PiperOrigin-RevId: 170312285