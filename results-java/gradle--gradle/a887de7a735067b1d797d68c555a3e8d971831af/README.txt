commit a887de7a735067b1d797d68c555a3e8d971831af
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Mar 19 17:18:18 2011 +0100

    (GRADLE-1446) Refactored the intra-task dependencies between configurer & generator task for eclipse plugin. Made it more gradle-like by using dependency mechanism instead of implicit method call inside EclipseConfigurer. EclipseConfigurer no longer configures the domain objects for generator tasks, other tasks do it now. ModelBuilder needs to run extra tasks now (pending refactoring)

    Pending:
    -documentation for GeneratorTask