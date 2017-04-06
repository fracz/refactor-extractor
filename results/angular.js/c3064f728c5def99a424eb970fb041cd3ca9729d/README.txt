commit c3064f728c5def99a424eb970fb041cd3ca9729d
Author: Shahar Talmi <shahar.talmi@gmail.com>
Date:   Sat Aug 30 04:07:14 2014 +0300

    refactor(ngModel): get rid of revalidate

    Since the validation was refactored we can now work out inside
    `$commitViewValue()` whether to ignore validation by looking at whether
    the input has native validators.

    Closes #8856