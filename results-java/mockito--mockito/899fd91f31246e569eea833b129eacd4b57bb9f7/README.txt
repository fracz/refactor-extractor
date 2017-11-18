commit 899fd91f31246e569eea833b129eacd4b57bb9f7
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sun Apr 8 21:18:27 2012 +0200

    Changed ReturnsSmartNulls so that it does not use MockMaker. Instead it uses the regular way of creating mocks. This also enables some further refactorings in the Invocations area.