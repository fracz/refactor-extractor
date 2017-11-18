commit ed6b45b2c9b6b64ed447a7aca376232ae3a628b2
Author: Irina.Chernushina <irina.chernushina@jetbrains.com>
Date:   Tue May 16 18:38:26 2017 +0200

    WEB-26859 PsiInvalidElementAccessException when call 'Remove unused export' in vue files
    Fix: do not format in the middle (before the actual refactoring). Element gets invalidated before the actual operation. Also, it does not have much sense.
    - in the platform part, I introduced additional callback to be called _after_ refactoring. I did not want to change the behaviour of other parts
    - since the element is being deleted, after-reformatting should remove extra new lines/spaces, with respect to whether the element was inline
    Special problem is evaluating the range where to remove new lines/spaces after the refactoring; in the presence of the fact that actually parent-of-the-parent of the passed element can be removed (if the was wrapping statement which does not contain no more meaningful elements)
    That's why I am creating the collection of contexts around the element being deleted, and for its parents
    One test became worse :( - with deleting also the js doc, which became empty
    (UnusedLocalSymbols6)
     Do not know how it managed to work before and I think in this particular case the extra newline does not cost the price of possible error, if we try to also process this

    (cherry picked from commit 9f08aa4)