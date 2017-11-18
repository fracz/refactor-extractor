commit 5b30ac1531909e8e89bc44e94fa380cc2fe2e5ab
Author: Ilya.Kazakevich <Ilya.Kazakevich@jetbrains.com>
Date:   Thu Dec 1 20:10:38 2016 +0300

    PY-21750: Fix flask creation for Plugin.

    * Plugin and PyCharm uses different APIs to create projects. This point was missed after last PythonProjectGenerator refactoring, but fixed now
    * See PythonProjectGenerator comments for description of this commit