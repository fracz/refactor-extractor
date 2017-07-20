commit e86c4a472bae7aeefb7f671c2cd8853fd35f8b82
Author: Jeroen van Oort <jvanoort@simplexis.nl>
Date:   Mon May 9 16:13:18 2016 +0200

    [5.2] Stabilized table aliases for self joins by adding count (#13401)

    * stabilized table aliases for self joins by adding count, in order to improve query cacheability

    * style fixes

    * changed prefix to laravel_reserved_ for self join alias