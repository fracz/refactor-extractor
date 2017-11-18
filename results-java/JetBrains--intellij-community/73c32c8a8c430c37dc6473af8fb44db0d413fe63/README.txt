commit 73c32c8a8c430c37dc6473af8fb44db0d413fe63
Author: Nikita Skvortsov <nikita.skvortsov@jetbrains.com>
Date:   Thu Aug 10 19:03:00 2017 +0300

    improve project/module level libraries generation for Gradle import

    Create a module-level dependency if the library's file is located under Gradle project. Create project level dependency otherwise.
    Avoid names collision: create new name, if existing library does not have reference to file