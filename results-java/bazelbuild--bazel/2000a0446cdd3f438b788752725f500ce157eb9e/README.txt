commit 2000a0446cdd3f438b788752725f500ce157eb9e
Author: fzaiser <fzaiser@google.com>
Date:   Thu Sep 14 11:24:46 2017 +0200

    Skylint: report missing documentation for a function's return value

    In addition to checking the function parameter documentation,
    skylint now also checks whether the return value is documented in a
    'Returns:' section in the docstring.

    The same restrictions as for parameters apply:
    - Private functions need no documentation
    - If a function has a single line docstring, it need not document the
      return value.

    In addition, I improved the docstring parsing:

    - Previously, the beginning and end of a section (e.g. 'Args:',
      'Returns:') were determined by line breaks. Now, they're determined by
      indentation and missing line breaks are reported. This change should
      make the docstring parser more robust.
    - Additional indentation is not warned against anymore.
      There are many situations where it makes sense, like example code.

    Both of these changes were motivated by the results of the linter on
    Skylark files "in the wild".

    RELNOTES: none
    PiperOrigin-RevId: 168660248