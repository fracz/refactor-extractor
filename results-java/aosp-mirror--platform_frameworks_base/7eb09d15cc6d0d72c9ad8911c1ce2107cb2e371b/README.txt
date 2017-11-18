commit 7eb09d15cc6d0d72c9ad8911c1ce2107cb2e371b
Author: Alan Viverette <alanv@google.com>
Date:   Fri Aug 14 11:36:01 2015 -0400

    Clean up TabWidget

    No functional changes, just refactoring and cleaning up comments.

    Actually, just one functional change so that setting the tab strip to
    @empty will actually remove the tab strip drawable. This makes it
    consistent with passing null to the accessor method.

    Change-Id: I6f5ba8bea4e8b30117de5f12a86957e995812cfe