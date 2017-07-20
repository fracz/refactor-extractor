commit 7019c7cf6c6af39c0f458769e20c3f9306477943
Author: Jeremy Benoist <jeremy.benoist@gmail.com>
Date:   Thu Dec 31 11:24:46 2015 +0100

    Add tagged services for import

    - list services in /import
    - add url to import service
    - ImportBundle routing are now prefixed by /import
    - optimize flush in each import (flushing each 20 contents)
    - improve design of each import
    - add more tests