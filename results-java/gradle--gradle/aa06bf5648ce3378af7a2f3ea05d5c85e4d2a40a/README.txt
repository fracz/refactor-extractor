commit aa06bf5648ce3378af7a2f3ea05d5c85e4d2a40a
Author: Adam Murdoch <adam@gradle.com>
Date:   Mon Feb 20 09:55:50 2017 +1100

    Improved collection of multiple listener notification errors, for improved error reporting. Also avoid creating a garbage `ArrayList` on each event, when the event succeeds (which they almost always do).