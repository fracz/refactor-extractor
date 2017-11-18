commit 15884a694059346078136db10cee76631abba6b7
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Oct 18 16:13:53 2013 +0200

    Pushed out the lock protocol to a separate class. For now, it's only a refactoring, without changes to the behavior. It should allow us to have different implementation of FileLockAccess. Refactorings to unit test coverage are needed plus some cleanup.