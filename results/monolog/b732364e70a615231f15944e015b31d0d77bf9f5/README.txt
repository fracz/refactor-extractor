commit b732364e70a615231f15944e015b31d0d77bf9f5
Author: Anton Nizhegorodov <hookman.ru@gmail.com>
Date:   Tue Dec 13 16:25:55 2016 +0200

    Slack improvements (#894)

    - [x] Exclude `extra`/`context`, `datetime`, `level` from message when attachment is used
    - [x] Use `ts` attachment key to display `datetime` considering user timezone
    - [x] [Support](https://github.com/Seldaek/monolog/pull/846#issuecomment-249528719) custom user images
    - [x] [Allow](https://github.com/Seldaek/monolog/pull/894#issuecomment-263532399) to setup username from slack
    - [x] [Improve](https://github.com/Seldaek/monolog/pull/846#issuecomment-261529198) array formatting within `context`/`extra`
    - [x] [Support](https://github.com/Seldaek/monolog/issues/745) `include_stacktraces` option when attachment is not used and always include stacktraces when attachment is used
    - [x] Support `extra`/`context` field exclusion
    - [x] Update tests