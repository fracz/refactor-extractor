commit 43b73156f44a61a07035d60df470551ccbebb5d1
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Fri Nov 18 12:31:06 2016 +0100

    Read replica catchup remake

    Introduce two states for transaction pulling and store copying respectively
    to make the catchup process cleaner and improve on the error handling.