commit e8941c0fe5217d2e705bad8253dc0162aff4c709
Author: IShotTheSheriff <enowacki@gmail.com>
Date:   Thu Nov 13 22:44:17 2014 +0100

    feat(ngModelController): add $setDirty method

    - extract existing functionality to public method: $setDirty
    - add tests to corresponding changes
    - refactor code to use extracted method

    Closes #10038
    Closes #10049