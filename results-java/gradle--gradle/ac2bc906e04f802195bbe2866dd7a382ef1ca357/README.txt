commit ac2bc906e04f802195bbe2866dd7a382ef1ca357
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Wed Jan 25 02:41:16 2012 +0100

    introduced ForkOptions.useAntForking switch

    - defaulted switch to `true` to bring back m7 (and earlier) behavior
    - renamed and improved ForkingJavaCompilerIntegrationTest