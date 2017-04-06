commit 94533e570673e6b2eb92073955541fa289aabe02
Author: Lucas Galfaso <lgalfaso@gmail.com>
Date:   Sun Aug 2 13:35:26 2015 +0200

    refactor(form): remove the use of the private setter function

    Remove the private `setter` function from $parse
    Replace the `setter` from the `form` directive with $parse

    Closes #12483