commit 92094c2e38f1c809b954ab1faa37781f5ff05da1
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Jan 9 21:52:49 2012 +0100

    Tooling api refactorings to avoid using reflection to figure out supported features...

    Started using consumer operation parameters in the consumer code. This way we less depend on the protocol types that are harder to change. This also allows me to implement the feature validation logic without reflection. Got rid of some unnecessary base class (AbstractLongRunningOperation) and used composition instead.