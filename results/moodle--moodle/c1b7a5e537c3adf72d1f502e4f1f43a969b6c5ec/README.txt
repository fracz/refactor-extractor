commit c1b7a5e537c3adf72d1f502e4f1f43a969b6c5ec
Author: martinlanghoff <martinlanghoff>
Date:   Wed Sep 19 07:08:12 2007 +0000

    accesslib: introducing make_context_subobj() - and refactor callers

    We had many copies of the subcontext building bit. Time to
    abstract it, and fix callers...