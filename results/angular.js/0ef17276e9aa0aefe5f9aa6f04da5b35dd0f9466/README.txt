commit 0ef17276e9aa0aefe5f9aa6f04da5b35dd0f9466
Author: Shahar Talmi <shahar.talmi@gmail.com>
Date:   Mon Apr 14 18:43:07 2014 +0300

    refactor(inputSpec): move call to `$digest` into `compileInput` helper

    It is reasonable to expect a digest to occur between an input element
    compiling and the first user interaction.  Rather than add digests to
    each test this change moves it into the `compileInput` helper function.