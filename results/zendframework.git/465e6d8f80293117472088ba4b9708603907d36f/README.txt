commit 465e6d8f80293117472088ba4b9708603907d36f
Author: Maks3w <github.maks3w@virtualplanets.net>
Date:   Thu May 10 14:42:10 2012 +0200

    [Json] Change unit test.

    The casting from string to float using (float) is not locale aware.

    Probably this function could be refactored implementing something like this
    http://www.php.net/manual/en/function.floatval.php#92563