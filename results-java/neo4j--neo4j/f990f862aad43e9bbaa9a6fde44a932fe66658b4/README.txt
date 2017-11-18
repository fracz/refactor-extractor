commit f990f862aad43e9bbaa9a6fde44a932fe66658b4
Author: Thomas Baum <thomas.baum@atns.de>
Date:   Mon Oct 29 15:25:26 2012 +0100

    add cypher statement logging

    - redirect dependencyResolver for Logger to kernel
    - refactor use CypherExecutor instead creating a ExecutionEngine