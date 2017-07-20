commit dec780a2f71340e2d1ce89e64fbff37a25713929
Author: Ryan McCue <me@ryanmccue.info>
Date:   Sun Aug 18 18:19:24 2013 +1000

    Store headers internally as arrays and refactor

    This lets us do more magical stuff in other places, since we might need
    case-insensitive dictionaries elsewhere. The internal use of arrays
    breaks one test at the moment, this will need to be fixed.