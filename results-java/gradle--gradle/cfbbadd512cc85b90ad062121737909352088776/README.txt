commit cfbbadd512cc85b90ad062121737909352088776
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Mar 19 15:38:40 2011 +0100

    (GRADLE-1446) Refactored the intra-task dependencies between configurer & generator task. Made it more gradle-like by using dependency mechanism instead of implicit method call inside IdeaConfigurer. Details:

    Details:
    -removed the hacky configuraiton-lifecycle stuff from GeneratorTask
    -IdeaConfigurer no longer configures the domain objects for generator tasks
    -new task task does it now
    -removed awkward 'model' state from IdeaModule/IdeaPlugin/IdeaProject. It was my earlier refactoring and didn't make much sense in a first place...

    Pending:
    -documentation for GeneratorTask
    -fix eclipse plugin