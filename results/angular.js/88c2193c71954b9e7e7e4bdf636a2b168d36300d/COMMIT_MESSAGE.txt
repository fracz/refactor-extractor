commit 88c2193c71954b9e7e7e4bdf636a2b168d36300d
Author: rodyhaddad <rody@rodyhaddad.com>
Date:   Thu Mar 6 15:57:24 2014 -0500

    refactor($interpolate): split .parts into .expressions and .separators

    BREAKING CHANGE: the function returned by $interpolate
    no longer has a `.parts` array set on it.
    It has been replaced by two arrays:
    * `.expressions`, an array of the expressions in the
      interpolated text. The expressions are parsed with
      $parse, with an extra layer converting them to strings
      when computed
    * `.separators`, an array of strings representing the
      separations between interpolations in the text.
      This array is **always** 1 item longer than the
      `.expressions` array for easy merging with it