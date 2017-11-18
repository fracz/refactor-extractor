commit a0a2c125cf3615575ad90975a80273c826b83f6f
Author: Andrzej Fiedukowicz <Andrzej.Fiedukowicz@teradata.com>
Date:   Fri Jul 1 10:56:17 2016 +0200

    Extract ScalarHeader class to cover basic scalar properties

    The class ScalarHeader that can be created from element annotations
    and keeps track of basic function informations like:
     - isHidden boolean
     - isDeterministic boolean
     - description

    Additionaly ScalarImplementationHeader was defined to keep track
    of all informations including name before Signature is created
    (for the time of Implementation parsing process).

    ScalarHeader is not part of Signature it is container for
    informations about function that are not part of Signature
    but are required for presto to perform some operations on function.

    The ScalarHeader class may and should be reused in other SqlScalarFunction
    based classes to keep things smooth and compact but it is not done
    as part of this refactoring.