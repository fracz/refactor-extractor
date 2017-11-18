commit e439504fc4b072485ecfbf897b1db6a4a958daac
Author: Will Bailey <wbailey@fb.com>
Date:   Sun Sep 7 08:13:22 2014 -0700

    API improvements

    - Springs with no tension continue to move when velocity is applied
      until friction causes them to stop if no end value is set.
    - setCurrentValue now performs setAtRest by default with a new method
      signature added to support the uncommon use-case of setting current
      value without setting at rest