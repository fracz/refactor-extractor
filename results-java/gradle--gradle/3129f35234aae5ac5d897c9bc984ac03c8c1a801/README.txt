commit 3129f35234aae5ac5d897c9bc984ac03c8c1a801
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Mar 7 15:25:10 2012 +0100

    Refactoring/simplification. Made the provider's AdaptedOperationParameters carry all the information regarding the run build / build model request. Thanks to that, some simplification was possible. Made the tests less suseptible to refactorings by refactoring the ConfiguringBuildAction.