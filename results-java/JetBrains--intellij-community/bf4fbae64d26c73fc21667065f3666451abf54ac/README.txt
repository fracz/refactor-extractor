commit bf4fbae64d26c73fc21667065f3666451abf54ac
Author: Andrey Vlasovskikh <andrey.vlasovskikh@jetbrains.com>
Date:   Thu Jun 30 20:35:49 2011 +0400

    Fixed move class or function refactoring for "import as" constructs (PY-3929)

    This is a partial fix. It doesn't rename module imports and qualified names.