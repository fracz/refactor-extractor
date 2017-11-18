commit 25c27c6d3a3680c6c3cf4d4e0a0dc0a1c18b82e8
Author: howard.li <howard.li@vipshop.com>
Date:   Thu Jul 7 18:32:30 2016 +0800

    STORM-2083 Blacklist scheduler

    Resolved conflict via Jungtaek Lim <kabhwan@gmail.com>

    This commit squashes the commits into one and commit messages were below:

    1. add apache header
    2. rename method
    3. move config define to Config
    4. code style fix
    5. change debug log level to debug

    remove all blacklist enable config

    remove unused default value

    1.storm blacklist code style, header and other bugs
    2.wrap blacklist scheduler in nimbus and rebase to master

    change blacklist-scheduler schedule method log level.

    rename some variables and refactor badSlots args

    add blacklist.scheduler to default.yaml

    1. removeLongTimeDisappearFromCache bug fix
    2. add unit test for removeLongTimeDisappearFromCache
    3. change blacklistScheduler fields to protected so it can be visited from sub-class and unit tests

    1. remove CircularBuffer and replace it with guava EvictingQueue.
    2. modify nimbus_test.clj to adapt blacklistScheduler
    3. comments, Utils.getInt, DefaultBlacklistStrategy.prepare with conf