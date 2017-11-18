commit 70f8aa3a5e0434a2486a050489fc6f836310429e
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Fri Aug 29 02:39:11 2014 +0400

    IDEA-129008 PushDialog: create PushSpec  only for @NotNull PushTarget, and add annotations.

    * nullable target could be only in MyRepoModel,in any other places PushTarget should be @NotNull;
    * generics updated, need to improve