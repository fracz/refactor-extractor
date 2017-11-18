commit 6e45198ec525de04b750d72ff7e472511d6cf793
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Jan 2 12:37:00 2012 +0100

    Tooling api refactoring and tidy-up...

    Made the loaders use internal consumer interface instead of the protocol interface. This way the code is easier to maintain as we have more flexibility in refactoring.