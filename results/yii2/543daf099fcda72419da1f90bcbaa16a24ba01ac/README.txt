commit 543daf099fcda72419da1f90bcbaa16a24ba01ac
Author: Carsten Brandt <mail@cebe.cc>
Date:   Mon Mar 30 18:46:48 2015 +0200

    Added Formatter:$numberFormatterSymbols

    this allows to specify custom symbols such as currency symbol.
    also improved error reporting in case intl fails

    fixes #7915, close #7920