commit 12d883f6b9b022d305c7303871a44e4d6bd4fae4
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Thu Jan 26 21:43:04 2017 +0100

    Introduce "server.servlet" configuration prefix

    This commit refactors the `ServerProperties` property keys and
    introduces a separate "server.servlet" namespace to isolate
    servlet-specific properties from the rest.

    Closes gh-8066