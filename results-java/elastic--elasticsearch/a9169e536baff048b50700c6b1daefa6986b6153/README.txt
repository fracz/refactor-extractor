commit a9169e536baff048b50700c6b1daefa6986b6153
Author: Simon Willnauer <simonw@apache.org>
Date:   Tue Aug 15 17:42:15 2017 +0200

    Several internal improvements to internal test cluster infra (#26214)

    This chance adds several random test infrastructure improvements that caused
    issues in on-going developments but are generally useful. For instance is it impossible
    to restart a node with a secure setting source since we close it after the node is started.
    This change makes it cloneable such that we can reuse it for a restart.