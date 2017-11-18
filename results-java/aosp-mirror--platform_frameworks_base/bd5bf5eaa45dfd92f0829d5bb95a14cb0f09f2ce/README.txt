commit bd5bf5eaa45dfd92f0829d5bb95a14cb0f09f2ce
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
    Change-Id: I7768f6111ada30edcb997a42940e5e336efe1cf2