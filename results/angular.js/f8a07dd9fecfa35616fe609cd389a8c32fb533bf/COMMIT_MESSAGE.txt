commit f8a07dd9fecfa35616fe609cd389a8c32fb533bf
Author: Martin Staffa <mjstaffa@googlemail.com>
Date:   Tue Aug 4 18:14:54 2015 +0200

    refactor(form, ngModel): streamline how controls are added to parent forms

    This delegates setting the control's parentForm to the parentForm's
    $addControl method. This way, the model controller saves one instance
    of looking up the parentForm controller. The form controller keeps two
    lookups (one for its own ctrl, one for the optional parent).

    This also fixes adding the parentForm in the following case:
    - a control is removed from a parent, but its corresponding DOM
    element is not destroyed
    - the control is then re-added to the form

    Before the fix, the control's parentForm was only set once during
    controller initialization, so the the parentForm would not be set on
    the control in that specific case.