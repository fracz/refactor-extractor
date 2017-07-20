commit f5ab85c393f7c745e7e7232630f47e6e1a0ef998
Author: Tobias Munk <schmunk@usrbin.de>
Date:   Tue Jan 10 11:42:26 2017 +0100

    updated debug settings (+56 squashed commits)
    Squashed commits:
    [c42f30c] updated base image, added docs & local test script
    [88f0c40] debug
    [6220c94] fixed network isolation
    [c63c7c3] test mssql only on test/mssql branch
    [74efc78] fixed isolation in after_script
    [02b895b] updated test setup
    [3335f39] updated retry
    [bd123b2] updated service checks
    [45e4c90] updated build
    [d54da7a] updated after_script
    [5a4c726] :factory: wait for mysql
    [08db878] fixed typo
    [ea53c1e] updated build stages
    [9807ce3] fixed typos
    [cf9f64e] fixed mssql testing
    [08001d6] added db create for mssql
    [62f6b65] run travis (gitlab simulation) only in travis branch
    [cf63da4] streamlined build
    [76808ac] updated test jobs
    [18d79b5] fixed test error
    [7b2bce6] updated build & composer.lock
    [244623a] updated build
    [86bd71b] fixed cleanup
    [86ab2e8] fixed cleanup
    [091d4b8] fixed tests
    [2d315b5] fixed build config
    [2913644] fixed project names
    [f53b823] refactored build config
    [5a791fb] refactored docker db-tests
    [b4479b0] revert
    [a975fa5] updated gitlab build
    [4e4e5e4] updated mssql setup
    [d6ff03b] added sleep workaround
    [578b102] removed host volumes in test
    [928f50b] fixed path
    [967ab10] updated tests
    [520f317] bootstrap cubrid
    [5f245e1] :factory: fixed cubrid tests
    [940dbbc] :factory: pinned cubrid version 9.3.6.0002
    [8d5ea69] :memo: dockerized test commands
    [9954b54] updated cubrid
    [fb3afac] updated docs
    [3f63ced] updated isolation
    [bdc3c83] :factory: build cubrid
    [b777911] :factory: disabled mssql, updated cubrid
    [36534e7] added travis-a-like stage
    [6546f02] updated GitLab build
    [c046096] updated test setup
    - added mssql and cubrid stack
    [5a0e635] disabled host-volume
    [75cf342] fixed test
    [8de0794] fixed testing
    [465d27a] added stages
    [63aa950] added script
    [68eecef] fixed typo
    [00e4b88] updated Docker build
    [f9072cc] added dockerized test setup