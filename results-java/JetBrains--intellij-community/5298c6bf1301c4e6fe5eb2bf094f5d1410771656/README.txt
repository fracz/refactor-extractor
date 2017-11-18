commit 5298c6bf1301c4e6fe5eb2bf094f5d1410771656
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Mon May 26 19:57:03 2014 +0400

    HgCommandExecutor refactoring

    * finally block added to stop appropriate socket server in any cases;
    * warnings deleted fromHgCommandResult and from PromptCommandExecutor and now considered as non-fatal errors;
    * message title changed in PromptCommandExecutor;
    * prompthooks extension arguments creation moved to a separate method;
    * log command method overrided for remote command execution to not provide password socket information outside