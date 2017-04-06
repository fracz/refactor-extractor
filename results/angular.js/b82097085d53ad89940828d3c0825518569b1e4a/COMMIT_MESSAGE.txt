commit b82097085d53ad89940828d3c0825518569b1e4a
Author: Georgios Kalpakas <kalpakas.g@gmail.com>
Date:   Fri Oct 7 21:14:44 2016 +0300

    perf(ngClass): avoid unnecessary `.data()` accesses, deep-watching and copies

    Includes the following commits (see #15246 for details):

    - **perf(ngClass): only access the element's `data` once**

    - **refactor(ngClass): simplify conditions**

    - **refactor(ngClass): move helper functions outside the closure**

    - **refactor(ngClass): exit `arrayDifference()` early if an input is empty**

    - **perf(ngClass): avoid deep-watching (if possible) and unnecessary copies**

      The cases that should benefit most are:

      1. When using large objects as values (e.g.: `{loaded: $ctrl.data}`).
      2. When using objects/arrays and there are frequent changes.
      3. When there are many `$index` changes (e.g. addition/deletion/reordering in large `ngRepeat`s).

      The differences in operations per digest include:

      1. `Regular expression (when not changed)`
         **Before:** `equals()`
         **After:**  `toClassString()`

      2. `Regular expression (when changed)`
         **Before:** `copy()` + 2 x `arrayClasses()` + `shallowCopy()`
         **After:**  2 x `split()`

      3. `One-time expression (when not changed)`
         **Before:** `equals()`
         **After:**  `toFlatValue()` + `equals()`*

      4. `One-time expression (when changed)`
         **Before:** `copy()` + 2 x `arrayClasses()` + `shallowCopy()`
         **After:**  `copy()`* + `toClassString()`* + 2 x `split()`

      5. `$index modulo changed`
         **Before:** `arrayClasses()`
         **After:**  -

      (*): on flatter structure

      In large based on #14404. Kudos to @drpicox for the initial idea and a big part
      of the implementation.

    Closes #14404

    Closes #15246