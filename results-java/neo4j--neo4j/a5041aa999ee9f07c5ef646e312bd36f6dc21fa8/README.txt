commit a5041aa999ee9f07c5ef646e312bd36f6dc21fa8
Author: Tobias Ivarsson <tobias.ivarsson@neotechnology.com>
Date:   Tue Aug 17 03:19:53 2010 +0000

    Refactored the Neo4j shell for discoverability.
    The shell server will now discover implementations of commands (App) and install them automatically using the Service interface.
    This means that any component can provide additional commands for the shell.

    Also refactored kernel to discover the shell using the same mechanism instead of using reflection.
    Also decoupled the JMX interface in the same way, by going through the Service interface.


    git-svn-id: https://svn.neo4j.org/components/kernel/trunk@5193 0b971d98-bb2f-0410-8247-b05b2b5feb2a