commit f4c423cea19858f9fa17e26f9c7447030f0ad8a0
Author: brunoais <brunoaiss@gmail.com>
Date:   Fri Mar 6 11:07:09 2015 +0000

    [ticket/13674] Change MySQL native total search results calculation

    This changes how the native FULLTEXT search calculates the total
    match number for MySQL.
    This should improve performance as there is one less query being made
    and it is being searched using the technique mentioned in the manual

    PHPBB3-13674