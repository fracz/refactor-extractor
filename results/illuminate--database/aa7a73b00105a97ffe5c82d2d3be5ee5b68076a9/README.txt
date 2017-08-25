commit aa7a73b00105a97ffe5c82d2d3be5ee5b68076a9
Author: Jeroen van Oort <jvanoort@simplexis.nl>
Date:   Mon May 9 16:13:18 2016 +0200

    [5.2] Stabilized table aliases for self joins by adding count (#13401)

    * stabilized table aliases for self joins by adding count, in order to improve query cacheability

    * style fixes

    * changed prefix to laravel_reserved_ for self join alias