commit 50ad8882830224463600879a4182e6536c810a2b
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Mon Mar 11 15:39:02 2013 +0400

    Subversion: commit with command line to https:// with client certificate supported.
    code slightly refactored to be more readable;
    tests for commit run locally for http, https
    SSL client certificate dialog to be initialized with stored client file value

    code notes: 1) now we pass "is password" parameter read from command line err output to switch between password and client certificate types
    2) we store client certificate path in servers file - as by spec, but svnkit doesn't do that somewhy
    3) we also store client certificate passphrase in Subversion native format, overriding svnkit behaviour, which seems to be wrong (realmstring -> client certificate path)