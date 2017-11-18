commit 23301afb8e763fd378f3fe13000ee42c19fa25a6
Author: Selim Cinek <cinek@google.com>
Date:   Tue Jan 31 14:37:14 2017 -0800

    Fixed an issue where heads-upped notifications would stay

    If a heads up notification was briefly removed right after
    it was added it could linger around in systemUI until
    the panel collapsed.
    We need to make sure not to apply this improvement if
    reordering isn't allowed, otherwise it will never time out

    Test: existing tests pass
    Bug: 34608075
    Merged-In: I7768f6111ada30edcb997a42940e5e336efe1cf2
    Change-Id: I7768f6111ada30edcb997a42940e5e336efe1cf2