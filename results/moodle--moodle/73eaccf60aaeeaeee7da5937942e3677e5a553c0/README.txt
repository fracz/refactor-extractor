commit 73eaccf60aaeeaeee7da5937942e3677e5a553c0
Author: Petr Skoda <commits@skodak.org>
Date:   Sun Jan 22 14:54:43 2012 +0100

    MDL-31081 limit query to one course in forum_get_subscribed_forums()

    This improves performance on the forum index page. Credit goes to Mark Nielsen.