commit 190863974927ec0c9c3a5951e6061e190e166897
Author: Shay Banon <kimchy@gmail.com>
Date:   Thu Aug 4 14:33:06 2011 +0300

    improve unicast discovery to use less resources by using dedicated thread pool capped with concurrent connects (defaults to 10)