commit d162f152b881068163e144a66fd5b9b7c916911d
Author: Pawel Kozlowski <pkozlowski.opensource@gmail.com>
Date:   Sun Dec 7 19:46:39 2014 +0100

    refactor($http): avoid re-creating execHeaders function

    The execHeaders function was being re-defined inside mergeHeaders
    function. Additionally it was mutating its arguments.

    Closes #10359