commit edb7e5bdf247a71b96dcfda06bab167778b39f57
Author: Gabriel Peal <gpeal@users.noreply.github.com>
Date:   Sat Feb 18 20:40:41 2017 -0800

    Converted all json to use opt* instead of get* (#128)

    * The parsing code is now cleaner.
    * There is a slight performance improvement using opt* instead of get*. I did some tests and didn't see a significantly measurable improvement but for large files or on certain devices, it may make a difference.