commit f4c5cde8af37e689b41e767f8ac52b0bbd841efe
Author: Zachary Tong <zacharyjtong@gmail.com>
Date:   Fri May 2 18:27:40 2014 -0400

    [TEST] Replace folded blocks with literal blocks

    The regex tests are formatted with blocks for readability.  Previously,
    they were formatted using folded style blocks (e.g. using `>`). Folded
    blocks convert newlines into spaces.  This is problematic for our regex,
    since comments can only be terminated with a newline.

    Effectively, anything after a comment will be commented out, making many
    of the regex "silently pass".

    This commit replaces them with scalar-style blocks (e.g. using `|`), which
    treats newlines as significant, and thus correctly terminates comments
    inside the regex.

    Also fixes a regex test (`cat.thread_pool/10_basic.yaml`) that started
    to fail after the block was fixed.  The test was missing a `\s+` before
    the closing newline.