commit be5ba7fa0d480a61eb7211846c30ffaabd5946c8
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Fri Aug 2 21:21:14 2013 +0400

    Migrate PropagationHeuristics from PSI to JavaElement

    Aside from refactorings, minor changes in logic are made:
    - to find out if a type of a value parameter is vararg ('ellipsis type'), we
      now check if the method is vararg and the parameter is its last parameter
      (instead of 'instanceof PsiEllipsisType')
    - 'visitedSuperclasses' is now a Set: this better reflects what it's supposed
      to represent. Also result check of the 'add()' method on a List was useless