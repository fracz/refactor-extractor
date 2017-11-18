commit d300e75eff0d5e54390400cbd3f80dc4cea8b617
Author: Gilles Debunne <debunne@google.com>
Date:   Mon Oct 17 13:37:36 2011 -0700

    Wrong word cut at end of lines with spaces

    Bug 5185017: when the line length is exceeded at a space character,
    we use the previous ok width, and the last word is wrapped to next line
    although it fits.

    This back-track also generates problem with the span parsing, where the
    spanStart indexes are no longer monotonuously increasing.

    Plus some refactoring in this code (unused parameters, calls to out())

    Change-Id: Ia8cd310a732752af3bd370bf0a16db23d40e83f2