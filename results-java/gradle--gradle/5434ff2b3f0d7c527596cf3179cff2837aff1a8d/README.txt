commit 5434ff2b3f0d7c527596cf3179cff2837aff1a8d
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Jan 2 11:19:59 2012 +0100

    Tooling api refactorings & tidy-up...

    -Added ways of building models different than ProjectVersion3. We need that because the BuildEnvironment is not a project so the internal design had to be changed. Due to the compatibility contract, the old way of getting the model is still in place.
    -Avoided using ProjectVersion3 and ConnectionVersion4 in some places so that the code is easier to refactor. Introduced a ConsumerConnection which is an internal consumer interface and it can adapt to the correct way of accessing the model from underlying protocol connection.