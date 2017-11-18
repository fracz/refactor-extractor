commit de99ce0812a25da06ef11938893a281cc0f25f6e
Author: sdh <sdh@google.com>
Date:   Tue Sep 12 11:55:45 2017 -0700

    Add a FunctionTypeI.Builder interface and refactor OTI's FunctionType.forgetParameterAndReturnTypes and FunctionTypeI.withReturnType to use it instead.

    In particular, migrating the coding conventions requires OTI to have a non-throwing implementation of withReturnType and NTI needs to expose a way to swap out parameter/return types for unknowns.  This unifies all of that functionality into a cleaner API.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=168413597