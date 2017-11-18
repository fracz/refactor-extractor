commit ab7ad3898c91dfc2afe6e776917f96dc8a096a44
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Wed Oct 26 18:22:04 2016 -0700

    The big keyguard transition refactor (5/n)

    Also save surface for the home task, so unlocking to the homescreen
    is as fast as before. Note that originally it was not possible to
    save surface for home task because of artifacts when clicking the
    home button. However, we added logic since then to disallow save
    surface if it's a different intent, which is the case when pressing
    the home button.

    Also consolidate some relayout for less overhead.

    Bug: 32057734
    Change-Id: I53ede527cb1ff438001d4023ee3740283ee302ee