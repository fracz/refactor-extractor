commit ceddc13f8253ec59c7ce0aca872c1eebf1f3b330
Author: MarkBaker <mark@lange.demon.co.uk>
Date:   Sun Apr 26 13:00:58 2015 +0100

    Implement a ColumnIterator, refactoring of RowIterator
    Currently this disables the setIterateOnlyExistingCells option

    TODO Re-Implement setIterateOnlyExistingCells logic with the new structure
    TODO Rationalise and abstract common methods in the iterator classes