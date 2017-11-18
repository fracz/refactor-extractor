commit ed2a54cfd336bb935f281c04509ecd48c8cf116d
Author: Clara Bayarri <clarabayarri@google.com>
Date:   Thu Feb 5 16:58:00 2015 +0000

    Floating Toolbars: Wrap the ActionMode creation in DecorView

    This change will allow us to create ActionMode representations on the
    fly after onCreateActionMode by using the Decorator pattern. The new
    ActionModeWrapper will be responsible for the creating the
    appropriate ActionMode depending on the type chosen by the client,
    and setting it up.

    Things pending that are NOT addressed by this CL:
    - ActionModes created by callback.onWindowStartingActionMode(). This
    includes all current usages in an existing ActionBar, as it is
    handled by Activity. This requires some additional refactoring.
    - Representing the floating type
    - Moving the view creation code specific to StandaloneActionMode
    from DecorView to ActionModeWrapper, decoupling DecorView from
    StandaloneActionMode completely
    - Supporting two ActionModes in parallel in DecorView, one of each type

    Change-Id: I1a8db711f53b771eac74f0e6496106acf1ca2727