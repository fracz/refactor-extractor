commit b4d0e3b1d6313b7f37852f42a2b8b54cc57af17e
Author: Christian Williams <christianw@google.com>
Date:   Mon Nov 14 16:53:34 2016 -0800

    Honor @Config(manifest=___) for Gradle projects

    An alternate manifest may be specified with `@Config(manifest = "AnotherManifest.xml")` for Gradle projects. The file should be placed in `test/resources`.

    `FileFsFile.from()` and `FileFsFile.join()` now ignore `'.'` path parts.

    Beginning to refactor `Config` to distinguish default field values from no-value-specified values.