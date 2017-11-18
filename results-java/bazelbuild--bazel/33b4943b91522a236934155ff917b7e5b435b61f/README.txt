commit 33b4943b91522a236934155ff917b7e5b435b61f
Author: brandjon <brandjon@google.com>
Date:   Thu May 4 18:58:52 2017 +0200

    Refactor "isMutable" -> "isFrozen"

    This helps readability, particularly since we also have "isImmutable" for SkylarkValues and in EvalUtils. I considered changing those to isFrozen as well, but we can leave it as-is since the terminology of freezing doesn't necessarily apply to non-Freezable things.

    Also rephrased some javadoc.

    RELNOTES: None
    PiperOrigin-RevId: 155090987