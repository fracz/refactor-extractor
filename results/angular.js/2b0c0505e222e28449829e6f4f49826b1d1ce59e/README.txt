commit 2b0c0505e222e28449829e6f4f49826b1d1ce59e
Author: Jason Bedard <jason+github@jbedard.ca>
Date:   Sun Oct 16 18:04:08 2016 -0700

    refactor($parse): move duplicate $parse interpreter/compiler logic into Parser
    - the construction of the AST is now in the Parser
    - the assigning of the literal and constant flags is now in the Parser
    - remove unused references to the lexer, $filter and options on the Parser