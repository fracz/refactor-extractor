commit 08e85a39099527afc8e3d50528a78bc1c00e1b60
Author: lutovich <konstantin.lutovich@neotechnology.com>
Date:   Thu Jul 21 12:43:12 2016 +0200

    Make KTI create StatementLocks

    KTI is not responsible for StatementLocks creation during `#initialize()`.
    StatementLocks are not passed in as a parameter.

    Commit also improves documentation around StatementLocks.