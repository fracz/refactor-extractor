commit fd6024ebf14f776dfdd5f95d5e1a56bc1b4a681f
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Mon Jul 13 11:52:42 2015 -0700

    Move and refactor Redis test server @Rule

    Move the Redis JUnit @Rule so that it can be used with
    SessionAutoConfigurationTests. Also refactored the internals a little.