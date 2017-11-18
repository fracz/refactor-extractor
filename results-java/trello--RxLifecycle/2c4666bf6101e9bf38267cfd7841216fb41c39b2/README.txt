commit 2c4666bf6101e9bf38267cfd7841216fb41c39b2
Author: Dan Lew <danlew42@gmail.com>
Date:   Wed Nov 2 08:54:57 2016 -0500

    Upgrade to RxJava 2

    The goal here is just to start the big transition, I'm sure there are
    lots of little polishes/improvements we can do once this is in.

    Some of the bigger changes:

    - Removed dependency on RxBinding since there is no RxJava 2-based
      RxBinding yet. Implementing our own ViewDetachesOnSubscribe was
      not that bad.

    - Rolled all Transformers into a single LifecycleTransformer, now that
      we can do so (due to composition-friendly interfaces).