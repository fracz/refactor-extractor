commit cc2cb11a63b3570c133e1fb37158c9eab3e43760
Author: Lóránt Pintér <lorant@gradle.com>
Date:   Wed Jun 8 19:57:15 2016 +0200

    Extend existing `TaskInputs`/`TaskOutputs` APIs instead of adding new ones

    Previously new APIs were introduced with names like `includeFile()` and `includeDir()` that were slightly improved versions of the existing `file()` and `dir()` methods. The additional functionality was that they returned an object that could be used to configure the freshly registered property; whereas the old methods used to return a reference to `TaskInputs` or `TaskOutputs` to allow chaining the calls. The old methods were previously deprecated in favor of the new ones.

    This commit removes the newly introduced `include*()` methods, and instead extends the existing ones. The tricky part is to keep compatible with the original method signatures while returning an object capable of configuring the property. To accomplish this, the property spec objects returned also implement `TaskInputs` and `TaskOutputs` to keep method chaining working. If chaining is used, a deprecation message is printed to warn the user to use `TaskInputs` or `TaskOutputs` directly instead.

    +review REVIEW-6038