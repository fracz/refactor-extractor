commit a868456e399a4802f1c3c9275c138d404a8f2177
Author: Andrii Rosa <Andriy.Rosa@TERADATA.COM>
Date:   Fri Feb 5 14:40:32 2016 +0100

    Migrate ADD/SUBSTRACT/DIVIDE Decimal operators

    In order to avoid extra cast of both decimal arguments to the same type
    ADD,SUBSTRACT and DIVIDE operators must accept different decimal types,
    e.g. (DECIMAL(3,2), DECIMAL(5,1)).

    We are going to remove arguments coercions to the same type because
    it is not required by the all operators. For instance Multiply operator
    does not require casting, because multiply operator has the semantic
    that doesn't require re-scaling.

    Compulsory casting of decimal arguments to the same type will be removed
    in a very next commit that incudes "matchAndBind" algorithm refactoring.