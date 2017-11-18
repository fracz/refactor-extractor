commit 5ec7617c7a3e502febfecc78f59b86c8ee6d1dcc
Author: Svetlana Isakova <svetlana.isakova@jetbrains.com>
Date:   Fri Apr 20 15:52:27 2012 +0400

    Call resolution refactoring (logic hasn't changed)

    - interfaces MemberPrioritizer, ResolutionCandidate introduced
    - CallTransformationStrategy will be responsible for variable with 'invoke' method as function resolve