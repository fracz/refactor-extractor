commit 03debd0a6eafa1277e7a15ca5d4748bfed5f5054
Author: Marc Delisle <marc@infomarc.info>
Date:   Sat Oct 4 08:27:50 2014 -0400

    Refactor: we had three similar functions, refactored into just one.

    Also, these functions returned false, or a string containing the type,
    but the string was only used to indirectly mean a true value; now
    we explicitely return true in this case.

    Signed-off-by: Marc Delisle <marc@infomarc.info>