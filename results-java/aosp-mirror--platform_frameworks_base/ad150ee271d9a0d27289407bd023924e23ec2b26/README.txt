commit ad150ee271d9a0d27289407bd023924e23ec2b26
Author: Yohei Yukawa <yukawa@google.com>
Date:   Wed Mar 16 17:22:27 2016 -0700

    Remove an unnecessary int to String conversion.

    This is a safe refactoring to remove an unnecessary int to String
    conversion in TextServicesSettings.

    Settings.Secure.SELECTED_SPELL_CHECKER_SUBTYPE is a integer value that
    indicates subtype ID (or SpellCheckerSubtype#hashCode() if the subtype
    ID is not specified), and we can just rely on
    Settings.Secure#putIntForUser() rather than converting int to String
    by ourselves then pass it to Settings.Secure#putStringForUser().

    Note that this change is still OK for existing users because
    Settings.Secure#putIntForUser() has been internally doing exactly the
    same thing.

    Bug: 27687531
    Change-Id: Ibcf12746f1295c12bec095300ea7f6ced0a51d09