commit a4f09d2b814595d7cca111cbc59d72fcf2dfb57d
Author: Daniel Mitterdorfer <daniel.mitterdorfer@gmail.com>
Date:   Wed Jul 20 13:13:57 2016 +0200

    Restore parameter name auto_generate_phrase_queries (#19514)

    During query refactoring the query string query parameter
    'auto_generate_phrase_queries' was accidentally renamed
    to 'auto_generated_phrase_queries'.

    With this commit we restore the old name.

    Closes #19512