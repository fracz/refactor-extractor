commit 0ef12aefe548e0d9472e2c23ab3374197a28b6bc
Author: Alan Viverette <alanv@google.com>
Date:   Tue Jan 12 09:47:09 2016 -0500

    Clean up LayerDrawable, refactoring only

    Uses consistent naming and ordering for padding and inset calculations.
    Updates documentation to include UNDEFINED_INSET as public API, since we
    return the value and will accept the value as a valid param. Flattens
    out RTL logic for readability.

    Change-Id: Ie704e2e5fc7f5763500ebe1217ddbbf4d0c5ef86