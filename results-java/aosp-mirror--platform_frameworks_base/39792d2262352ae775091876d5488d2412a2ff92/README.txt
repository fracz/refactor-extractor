commit 39792d2262352ae775091876d5488d2412a2ff92
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Aug 19 18:01:52 2010 -0700

    Fix bugs with granting permissions through onNewIntent().

    It would grant the permission to the temporary ActivityRecord,
    not the real one, so it never got cleaned up.

    Also allow granting of permissions to services because...  well,
    it would be really really useful.  And it introduces some
    refactoring that we'll need to support cut/paste.

    Change-Id: If521f509042e7baad7f5dc9bec84b6ba0d90ba09