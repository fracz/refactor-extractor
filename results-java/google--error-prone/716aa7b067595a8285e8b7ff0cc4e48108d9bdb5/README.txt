commit 716aa7b067595a8285e8b7ff0cc4e48108d9bdb5
Author: cushon <cushon@google.com>
Date:   Thu Apr 27 11:24:39 2017 -0700

    Avoid subclassing CompilationTask in BaseErrorProneJavaCompiler

    Instead, use a TaskListener to apply the refactoring before the
    compilation was about to end. This allows clients to depend on
    implementation details of JavacTaskImpl if they want to do so.

    Fixes #608

    MOE_MIGRATED_REVID=154446890