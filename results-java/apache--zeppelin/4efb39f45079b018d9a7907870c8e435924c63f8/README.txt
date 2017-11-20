commit 4efb39f45079b018d9a7907870c8e435924c63f8
Author: Lee moon soo <moon@apache.org>
Date:   Thu Jun 23 15:04:43 2016 -0700

    [ZEPPELIN-1046] bin/install-interpreter.sh for netinst package

    ### What is this PR for?
    Implementation of bin/install-interpreter.sh for netinst package which suggested in the [discussion](http://apache-zeppelin-users-incubating-mailing-list.75479.x6.nabble.com/Ask-opinion-regarding-0-6-0-release-package-tp3298p3314.html).

    Some usages will be

    ```
    # download all interpreters provided by Apache Zeppelin project
    bin/install-interpreter.sh --all

    # download an interpreter with name (for example markdown interpreter)
    bin/install-interpreter.sh --name md

    # download an (3rd party) interpreter with specific maven artifact name
    bin/install-interpreter.sh --name md -t org.apache.zeppelin:zeppelin-markdown:0.6.0-SNAPSHOT
    ```

    If it looks fine, i'll continue the work (refactor code, and add test)

    ### What type of PR is it?
    Feature

    ### Todos
    * [x] - working implementation
    * [x] - refactor
    * [x] - add test

    ### What is the Jira issue?
    * Open an issue on Jira https://issues.apache.org/jira/browse/ZEPPELIN/
    * Put link here, and add [ZEPPELIN-*Jira number*] in PR title, eg. [ZEPPELIN-533]

    ### How should this be tested?
    Outline the steps to test the PR here.

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update?
    * Is there breaking changes for older versions?
    * Does this needs documentation?

    Author: Lee moon soo <moon@apache.org>
    Author: AhyoungRyu <fbdkdud93@hanmail.net>

    Closes #1042 from Leemoonsoo/netinst and squashes the following commits:

    f81d16e [Lee moon soo] address mina's comment
    049bc89 [Lee moon soo] Update docs
    7307c67 [Lee moon soo] Merge remote-tracking branch 'AhyoungRyu/netinst-docs' into netinst
    7e749ad [Lee moon soo] Address mina's comment
    0eedd2a [AhyoungRyu] Address @minahlee feedback
    13f2d04 [Lee moon soo] generate netinst package
    03c664e [AhyoungRyu] Add a new line
    5d0a971 [AhyoungRyu] Revert install.md to latest version
    13899fb [AhyoungRyu] Reorganize interpreter installation docs
    4c1f029 [Lee moon soo] Proxy support
    9079580 [Lee moon soo] fix artifact name
    1077296 [Lee moon soo] update test
    aebca17 [Lee moon soo] Add docs
    d547551 [Lee moon soo] Remove test entries
    6ee06b8 [Lee moon soo] Make DependencyResolver in zeppelin-interpreter module not aware of ZEPPELIN_HOME
    7b1b36a [Lee moon soo] update usage
    49f0568 [Lee moon soo] Add conf/interpreter-list
    1b558fd [Lee moon soo] update some text
    ec7d152 [Lee moon soo] add tip
    2c81a3f [Lee moon soo] update
    78a7c52 [Lee moon soo] Refactor and add test
    47f5706 [Lee moon soo] Install multiple interpreters at once
    38e2556 [Lee moon soo] Initial implementation of install-interpreter.sh