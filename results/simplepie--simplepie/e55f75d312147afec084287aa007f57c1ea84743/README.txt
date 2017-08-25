commit e55f75d312147afec084287aa007f57c1ea84743
Author: Alexandre Alapetite <alexandre@alapetite.fr>
Date:   Mon Feb 17 18:21:34 2014 +0100

    [FreshRSS] New function add_attributes()

    Just like there is a strip_attributes() function, here is a new
    add_attributes() which works the same way, except it is to add
    attributes instead of removing them.

    Just like strip attributes(), add_attributes() comes with some default
    settings, namely to add a few HTML5 attributes:
    ```html
    <audio preload="none" />
    <video preload="none" />
    <iframe sandbox="allow-scripts allow-same-origin" />
    ```

    Watch out that I have changed the signature of the protected functions
    strip_tag() and strip_attr() for performance reasons (to avoid a loop of
    new DOMXPath()). If this is a problem, I could propose a patch with the
    old signatures.

    This is a first attempt to report upstream some improvements made for
    the FreshRSS news reader http://freshrss.org. If there is interest,
    there will be more.