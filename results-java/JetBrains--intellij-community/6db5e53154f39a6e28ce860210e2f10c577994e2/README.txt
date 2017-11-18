commit 6db5e53154f39a6e28ce860210e2f10c577994e2
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Tue May 20 15:30:36 2014 +0400

    HgCommandExecutor refactoring (IDEA-119330 Reorganize mercurial command execution )

    * HgCommandExecutor divided into 3 different classes for local, remote and prompt commands;
    * python script modified to not process warnings if we don't need them;
    * annotations added