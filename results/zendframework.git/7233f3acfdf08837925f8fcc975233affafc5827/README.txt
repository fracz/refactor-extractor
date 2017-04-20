commit 7233f3acfdf08837925f8fcc975233affafc5827
Author: Artur Bodera <abodera@gmail.com>
Date:   Tue Mar 6 14:04:56 2012 +0100

    Introduce Stdlib\ArrayTools, refactor other components to use it.

    - add Stdlib\ArrayTools which combines the following:
        - Stdlib\RecursiveArrayMerge
        - Stdlib\IteratorToArray
        - Stdlib\IsAssocArray
    - add ArrayTools array testing functionality:
        - ::isList()
        - ::isHashTable()
        - ::hasNumericKeys()
        - ::hasIntegerKeys()
        - ::hasStringKeys()
    - add more exhaustive unit tests for ArrayTools including edge-cases for different types of arrays