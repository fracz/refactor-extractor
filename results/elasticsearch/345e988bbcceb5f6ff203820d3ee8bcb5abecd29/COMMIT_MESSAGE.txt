commit 345e988bbcceb5f6ff203820d3ee8bcb5abecd29
Merge: c3078f4 554bf2c
Author: Simon Willnauer <simon.willnauer@elasticsearch.com>
Date:   Tue Mar 15 09:17:43 2016 +0100

    Merge pull request #17072 from s1monw/add_backwards_rest_tests

    Add infrastructure to run REST tests on a multi-version cluster

    This change adds the infrastructure to run the rest tests on a multi-node
    cluster that users 2 different minor versions of elasticsearch. It doesn't implement
    any dedicated BWC tests but rather leverages the existing REST tests.

    Since we don't have a real version to test against, the tests uses the current version
    until the first minor / RC is released to ensure the infrastructure works.

    Given the amount of problems this change already found I think it's worth having this run with our test suite by default. The structure of this infra will likely change over time but for now it's a step into the right direction. We will likely want to split it up into integTests and integBwcTests etc. so each plugin can have it's own bwc tests but that's left for future refactoring.