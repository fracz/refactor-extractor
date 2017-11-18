commit 054af3aea175ec14c678787f88f5b31978ab7b3a
Author: Jacob Hansson <jakewins@gmail.com>
Date:   Mon Feb 8 16:17:00 2016 -0600

    Introduce a neo4j-security module, for auth, encryption and so on.

    - Move the AuthManager down to neo4j-security, as a first step on
      a larger planned refactoring.

    - Also factor out a neo4j-common module for common infrastructure
      that are currently spread across varios modules. Moving a small
      portion of that spread-out code only to support the AuthManager.