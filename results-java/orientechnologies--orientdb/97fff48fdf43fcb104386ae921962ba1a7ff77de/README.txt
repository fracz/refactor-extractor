commit 97fff48fdf43fcb104386ae921962ba1a7ff77de
Author: lvca <l.garulli@gmail.com>
Date:   Wed Jul 24 14:11:42 2013 +0200

    Some improvements on collections: size(), remove() and removeAll()

    introduced the new interface OSizeable to know the size of certain
    iterators without browsing them. New removeAll() SQL function to remove
    all the occurrencies. Now remove() removes only the first one