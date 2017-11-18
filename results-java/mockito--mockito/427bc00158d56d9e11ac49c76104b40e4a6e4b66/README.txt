commit 427bc00158d56d9e11ac49c76104b40e4a6e4b66
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Apr 9 17:58:36 2012 +0200

    Unfortunetly meaty checkin but things got out of hand during Easter... Further work on exposing an MockSettingsInfo. Encountered few obstacles and did some refactorings along the way. Stopped using the class array in few place in favor of a Collection - MockMaker interface has changed in due course. Fixed the javadoc a bit. There's still long way to get the MockSettings cleaned up but I'm getting there. The purpose is to avoid casting to our internal type (MockSettingsImpl) and use the information provided by the interface MockSettingsInfo. MockSettingsInfo is an interface exposed when we created MockMaker API.