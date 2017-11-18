commit 87187d9a39495f95d1dc8fc410a2c7faac507769
Author: Ryan Lubke <ryan.lubke@oracle.com>
Date:   Thu Jun 6 16:46:43 2013 -0700

    - Refactoring of connection and proxy code.
    - Update to Grizzly 2.3.4-SNAPSHOT.  This build includes changes that allows Filter instances to be shared between FilterChain instances.
    - Remove dead code.
    - Remove proxy knowledge from client filter.

    TODO - improve documentation.