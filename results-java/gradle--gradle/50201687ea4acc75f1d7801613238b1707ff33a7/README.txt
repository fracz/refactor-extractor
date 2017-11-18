commit 50201687ea4acc75f1d7801613238b1707ff33a7
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Mon Jan 9 19:26:41 2012 +0100

    reworked checkstyle plugin

    notably:
    - turn into its own plugin
    - introduce checkstyle configuration (don't hardcode the checkstyle version)
    - configure via extension object
    - improve tests and move them into code-quality project
    - implement in groovy