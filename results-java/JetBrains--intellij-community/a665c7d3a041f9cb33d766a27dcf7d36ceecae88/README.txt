commit a665c7d3a041f9cb33d766a27dcf7d36ceecae88
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Thu Mar 16 16:27:58 2017 +0100

    improve highlighting for WEB-25837 JSON Schema incorrectly validate oneOf condition
    - merge base and oneOf/anyOf/allOf schemas (instead of checking them in order)
    - walker reports when oneOf/anyOf variants found -> so that annotator receives this information (did not work fine for specific level of nested definitions)
    - more levels of merging, for instance:
    (oneOf -> (from definitions)[allOf, anyOf]) or allOf -> (from definitions)[oneOf, allOf]