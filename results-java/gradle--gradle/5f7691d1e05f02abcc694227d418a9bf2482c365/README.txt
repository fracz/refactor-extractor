commit 5f7691d1e05f02abcc694227d418a9bf2482c365
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Jan 12 14:59:32 2012 +0100

    Improving the tooling api coverage for concurrency.

    The test that was exercising the concurrent scenario with different target gradle versions now truly uses the 'previous' version, instead of hardcoded M7. Introduced ReleasedVersions type that holds that information (this can be improved further). Unignored the concurrent test because the reason for failure we a red herring (I think).