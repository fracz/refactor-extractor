commit 2dc4883f0eb8b68b0f53d538aa93e701d58ef3e1
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Fri Aug 1 18:25:12 2014 +0200

    refactor shutdown for ES connection failure during startup

    there were some livelocks involving the latches used in starting the inputs

    fixes #517