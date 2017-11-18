commit ba477939f0ae38926b4b0a6501a2371acc612433
Author: Felipe Leme <felipeal@google.com>
Date:   Wed Dec 9 11:04:59 2015 -0800

    Improved test case by checking for dangling service.

    This check will make it easier to refactor how the bugreport is shared, which is a requirement for showing the bugreport details window.

    BUG: 25794470
    Change-Id: If29f0515586c6680a44e0d52c4fc587808e668aa