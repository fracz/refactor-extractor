commit 3820a24afc9904becf8b5262b9ead4cd3c4d468c
Author: Julia Beliaeva <Julia.Beliaeva@jetbrains.com>
Date:   Thu Feb 16 23:49:26 2017 +0300

    [file-history] move history actions into separate package, create special history action classes when actions are reused

    This is the first step in history actions refactoring. Since they works a little bit differently than conventional actions, I need to use separate action classes. This commit introduces action classes and groups them in a separate package. There is still a great deal of duplication in the code.