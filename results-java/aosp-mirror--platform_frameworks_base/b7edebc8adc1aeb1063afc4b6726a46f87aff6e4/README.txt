commit b7edebc8adc1aeb1063afc4b6726a46f87aff6e4
Author: Yohei Yukawa <yukawa@google.com>
Date:   Mon Apr 11 01:38:23 2016 -0700

    Fix inefficient CursorAnchorInfo#hashCode().

    It turns out that the current CursorAnchorInfo#equals() is quite
    inefficient because our CursorAnchorInfo#hashCode() tries to use almost
    all the fields.  Even worse, as Matrix#hashCode() is hard-coded to 44,
    we get the same hashCode() when comparing two CursorAnchorInfo objects
    that are different only in transformation Matrix after such a complex
    hash calculation.

    In the real world scenarios, most likely calculation hash code only from
    Matrix and composing text would be good enough for our use case, because
    the former can cover UI scrolling scenario and the latter can cover the
    text typing scenario.  More complex hash calculation is probably
    inefficient.

    With this CL, CursorAnchorInfo#hashCode() is pre-calculated only from
    those two fields, and carefully reorder comparisons in
    CursorAnchorInfo#equals() to improve the likelihood of returning false
    with fewer comparisons.

    Bug: 28105733
    Change-Id: Id896adeab5ffe87ceddb2c2762d6d91475e28ec4