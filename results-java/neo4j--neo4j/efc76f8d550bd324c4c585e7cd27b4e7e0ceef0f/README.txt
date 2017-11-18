commit efc76f8d550bd324c4c585e7cd27b4e7e0ceef0f
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Wed Sep 4 15:24:58 2013 +0200

    Corrects DiffSets problems

    There were problems in how the added/removed parts of DiffSets were
    populated and managed in general. The problems were exposed in that
    the 'remove' part contained invalid and/or too few entries and the
    'added' part contained too many entries. In the property scenario
    the diff set was used to remove the old property and add the new.
    Add and remove were issued separated from each other, which made those
    step on each others toes.

    These things were changed:
    o Introduced DiffSets#replace which does proper add/remove semantics
      knowing that one value replaces another.
    o Removed null from the added/removed sets management by using empty sets
      in place of null. This makes for less checks, less code, more
      intuitive code and actually less objects being created since a set
      may not need to be created to remove something that it doesn't have.
    o All *ArrayProperty (extends SafeProperty) classes have their value()
      method return a clone of the actual array. This fixes a problem with the
      initial test used to drive all these changes, where the test changed and
      reused an array property value read from a node.
    o Safe equals() on LazyArrayProperty and clearly describes why the type
      member field needs no synchronization or volatility; both for
      readability and for reasoning about potential future changes to
      synchronization thereabout.

    co-author: Tobias Lindaaker, @thobe