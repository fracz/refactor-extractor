commit 70c2499e8b08d15974270f8adf38b9af89c5d2cd
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Aug 20 00:38:18 2011 +0200

    GRADLE-1748. Started refactoring some stuff related to the daemon folder. The idea is to keep all that logic in a single place. The benefits include using the content of the daemon folder in tests for assertions.