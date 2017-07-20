commit 1a8feb646edca8a01408bf8a7d2ed7981769ae5b
Author: Charles Sanquer <charles.sanquer@gmail.com>
Date:   Mon Mar 10 09:07:23 2014 +0100

    add Maximum Timestamp option to get always same unix timestamp when using a fixed seed

    fix and improve unit tests when setting endMaximumTimestamp

    change static maximumEndTimestamp to an optional parameter $max in each
    datetime methods

    some improvements to DateTimeProvider max timestamp