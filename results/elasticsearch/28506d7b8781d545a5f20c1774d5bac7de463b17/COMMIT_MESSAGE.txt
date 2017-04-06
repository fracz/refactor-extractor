commit 28506d7b8781d545a5f20c1774d5bac7de463b17
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Jun 23 18:03:43 2015 +0200

    Revisited the queries already refactored, corrected and aligned some of
    the codebase based on the conventions that we decided to follow. Also including
    some cosmetic fixes (making members final where possible, avoiding this
    references outside of setters/getters).

    In addition to that this PR changes:

    * prevent NPEs in doXContent when rendering out nested queries that are null. We now render out empty object ({ }) which then gets parser to null to be consistent with queries than come only through the rest layer
    * prevent adding nested null queries to collections of clauses like in BoolQueryBuilder
    * add validate() method to all builders (even when empty)

    Closes #11834