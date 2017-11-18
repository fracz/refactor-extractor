commit b5efb742e795de758b9d5d071baa726565e5928b
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Thu Aug 14 16:12:53 2014 +0200

    Add support for reading 2.1 log files
    Such a feature is needed to upgrade from 2.1 to 2.2 when using HA
    Add a test case for upgrading from 2.1 to 2.2 in all scenarios: embedded, server, and HA
    Various cleanups and small improvements