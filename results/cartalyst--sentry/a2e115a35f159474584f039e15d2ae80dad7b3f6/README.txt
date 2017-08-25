commit a2e115a35f159474584f039e15d2ae80dad7b3f6
Author: Ben Corlett <bencorlett@me.com>
Date:   Fri Jan 18 16:55:10 2013 +1100

    Changing the way users are stored in the session / cookie to improve security and also fix bug where user would still be authenticated after it was deleted from the database.

    Signed-off-by: Ben Corlett <bencorlett@me.com>