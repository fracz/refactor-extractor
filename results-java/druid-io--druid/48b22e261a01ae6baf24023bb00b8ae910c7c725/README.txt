commit 48b22e261a01ae6baf24023bb00b8ae910c7c725
Author: John Zhang <zhxiaog@outlook.com>
Date:   Wed Dec 14 02:03:22 2016 +0800

    support atomic writes for local deep storage (#3521)

    * Use atomic writes for local deep storage

    * fix pr issues

    * use defaultObjMapper for test

    * move tmp pushes to a intermediate dir

    * minor refactor