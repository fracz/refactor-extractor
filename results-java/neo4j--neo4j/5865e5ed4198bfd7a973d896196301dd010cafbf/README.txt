commit 5865e5ed4198bfd7a973d896196301dd010cafbf
Author: peterneubauer <peter@neubauer.se>
Date:   Tue Dec 11 14:09:42 2012 +0100

    Tinkerpop/Gremlin refactoring

    pull out gremlin-shell to a separate plugin
    make session creators discoverable via service loaders
    move representation logic to the plugin
    move tests
    adapt tests and documentation
    adjusting non-compiling files, adding server config to enable gremlin shell.