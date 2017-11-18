commit 652046954c9479a91cb7f07014d805525091a0b1
Author: tomlu <tomlu@google.com>
Date:   Fri Aug 4 17:41:54 2017 +0200

    Move most test options from BuildConfiguration to TestConfiguration.

    --test_env isn't moved in this CL since it's exposed to Skylark via BuildConfiguration, making it a somewhat riskier refactor.

    PiperOrigin-RevId: 164266168