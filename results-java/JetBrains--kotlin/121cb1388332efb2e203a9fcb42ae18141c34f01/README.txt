commit 121cb1388332efb2e203a9fcb42ae18141c34f01
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Mon Apr 8 20:11:14 2013 +0400

    Minor refactoring in CodegenBinding

    - CLASS_FOR_FUNCTION slice was used for two purposes: to record closure classes
      for functions and for scripts. Separate this slice into two slices:
      CLASS_FOR_FUNCTION and CLASS_FOR_SCRIPT
    - add classNameForAnonymousClass() overloaded method to CodegenBinding, taking
      a function descriptor (not a PSI element)