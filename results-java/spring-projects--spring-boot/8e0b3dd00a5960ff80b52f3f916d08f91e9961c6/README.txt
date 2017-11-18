commit 8e0b3dd00a5960ff80b52f3f916d08f91e9961c6
Author: Phillip Webb <pwebb@gopivotal.com>
Date:   Mon Jul 28 23:17:34 2014 -0700

    Rename some ConditionalOnProperty attributes

    Rename the newly introduced @ConditionalOnProperty `match` and
    `defaultMatch` attributes to `havingValue` and `matchIfMissing`.

    Also added a new `name` attribute as an alternative to `value` to
    aid readability.

    Closes gh-1000