commit db8adb798394af6381974444bfa0c47f4b30f0fb
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Mon Apr 17 15:27:52 2017 +0900

    NsdManager: add unit tests

    This prepares some refactoring and the addition of a timeout to
    resolveService.

    Test: new tests pass
    Bug: 37013369, 33298084
    Change-Id: Ie8277bd5983278507bfa70495b4ce7d13895b24b