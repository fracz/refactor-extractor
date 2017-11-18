commit 34ff521d1fee9b48c46b8841d6e45bbe866c3bb1
Author: Narayan Kamath <narayan@google.com>
Date:   Thu May 21 10:50:35 2015 +0100

    Fix application moves.

    We don't dex2oat during application moves, so we must scan
    the package again scanPackageDirtyLI to deduce its ABI.

    This is unnecessary (since a move cannot change ABIs), but we
    need some additional refactoring to avoid a second scan.

    bug: 21337469

    Change-Id: I3e9dfd5db1c928847f9d527dc15d29a05ff40e7d