commit c135f0a5fa515f62dc2b98f625262419833c7e29
Author: Emil Eifrem <emil@neotechnology.com>
Date:   Sun May 27 08:35:45 2007 +0000

    Integrated a remote neo shell into the core distribution:

       o Moved "neo shell" from the neo-utils project to the neo project in package
         org.neo4j.impl.shell.
       o Added a dependency on the generic Neo4j "shell" project. This dependency
         should later be refactored to a "soft dependency": required at compile-time
         but will be used only if detected at runtime.
       o Removed neo shell's dependency on windh-utils.
       o Made some output polish on the Ls and Pwd apps.
       o Added an enableRemoteShell() method to EmbeddedNeo that starts a
         NeoShellServer and binds it to an RMI registry on localhost.


    git-svn-id: https://svn.neo4j.org/projects/neo/trunk@15 0b971d98-bb2f-0410-8247-b05b2b5feb2a