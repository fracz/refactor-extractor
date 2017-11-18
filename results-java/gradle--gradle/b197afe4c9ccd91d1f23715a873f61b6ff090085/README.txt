commit b197afe4c9ccd91d1f23715a873f61b6ff090085
Author: Cedric Champeau <cedric@gradle.com>
Date:   Wed Apr 26 10:27:00 2017 +0200

    Share the same root component metadata builder with all configurations

    This commit refactors `DefaultConfiguration` to share a single root component metadata builder instance between all configurations. Detached
    configurations get their own builder, and nothing is cached yet.