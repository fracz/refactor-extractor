commit a666267179496a85d219e479a8a7ef4616449907
Author: nik <Nikolay.Chashnikov@jetbrains.com>
Date:   Thu Sep 14 19:59:19 2017 +0300

    project structure: detect JAR files containing native libraries only as classes roots (IDEA-135750)

    FileTypeBasedRootFilter refactored to DescendentBasedRootFilter to allow this.