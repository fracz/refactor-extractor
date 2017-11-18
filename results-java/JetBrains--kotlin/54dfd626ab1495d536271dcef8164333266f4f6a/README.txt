commit 54dfd626ab1495d536271dcef8164333266f4f6a
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Sat Jun 13 03:43:20 2015 +0300

    CLI: improve diagnostic message format

    - render the whole line where the error/warning points to, if any, and another
      line with '^', like other compilers do
    - lowercase diagnostic severity
    - decapitalize the message if it doesn't start with a proper name