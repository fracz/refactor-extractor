commit 0ed9897b0302074268c09de2b2c390f402579423
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Thu Sep 29 18:51:40 2016 +0300

    Minor refactorings related to PackagePartProvider and implementations

    - Document what exactly should findPackageParts return
    - Make EMPTY a named object instead of a val in the companion
    - Do not use JvmPackagePartProvider in tests where Empty works fine
    - Add a couple default values to arguments of setupResolverForProject