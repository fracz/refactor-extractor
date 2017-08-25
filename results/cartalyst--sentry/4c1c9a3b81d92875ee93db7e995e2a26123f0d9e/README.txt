commit 4c1c9a3b81d92875ee93db7e995e2a26123f0d9e
Author: Ben Corlett <bencorlett@me.com>
Date:   Thu Feb 14 09:27:15 2013 +1100

    Refactoring how hashers are set on user models. They're now static, which should see a performance improvement and less boilerplate code.

    Signed-off-by: Ben Corlett <bencorlett@me.com>