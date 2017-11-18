commit 9d4b4d9aead6b11f260c34862f0d63e28c016185
Author: Jake Wharton <jw@squareup.com>
Date:   Sun Jan 4 23:06:44 2015 -0800

    Take no action if transferring zero bytes.

    This removes the logic from inside the loop for much better readability.