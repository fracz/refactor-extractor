commit 49f4176b0b835936587d378180aee4b459dae859
Author: Ekaterina Tuzova <Ekaterina.Tuzova@jetbrains.com>
Date:   Thu May 9 20:07:30 2013 +0400

    fixed PY-9753 Change Signature Refactor - removing an argument creates a mess in the calls to the refactored method

    do not add keyword parameter to call if it has default value in signature