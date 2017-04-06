commit 3b4bfa177103986653fabf5a02b20cf97781830a
Author: Martin Staffa <mjstaffa@gmail.com>
Date:   Mon Jun 6 17:03:59 2016 +0200

    refactor(ngMock): extract browserTrigger from ngScenario

    `ngScenario` is deprecated, and we expect to remove it from the project in 1.6,
    but we use the `browserTrigger` helper throughout our unit tests.

    So in preparation for removing `ngScenario` we are relocating `browserTrigger`
    to the `ngMock` folder.

    Although many people are using browserTrigger in their own application testing
    we are still not yet ready to make this a public API; so developers use
    this helper at their own risk.

    Closes #14718

    BREAKING CHANGE

    Although it is not a public API, many developers are using `browserTrigger`
    in their own application testing. We have now moved this helper from
    `ngScenario/browserTrigger.js` to `ngMock/browserTrigger.js`.