commit cab8d8a3e669e2ab65f3e7989a3b7aa77302643f
Author: cushon <cushon@google.com>
Date:   Tue Aug 1 15:11:13 2017 -0700

    Improve field renaming / field prefix refactoring

    * use CONSTANT_CASE for static final fields or well-known immutable type
    * make variable renaming more robust to handle e.g. c-style arrays and
      multi-variable declarations

    MOE_MIGRATED_REVID=163894328