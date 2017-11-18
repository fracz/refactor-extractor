commit 328f47189cd90328809d51982b9d634333ed06ec
Author: Sven Schindler <SvenSchindler@users.noreply.github.com>
Date:   Wed Jan 25 12:56:16 2017 +0100

    Use percent encoding in OAuth according to rfc 5849 section 3.6 (#1332)

    * Use percent encoding according to rfc 5849 section 3.6  for oauth signature generation so as to allow url paths such as foo/*bar/

    * add test for oauth calculator and asterisk in path

    * improve testability for oauth signature generation