commit 6b312a09fcea77651680f63a876ccd4ea378393f
Author: Adrian Roos <roosa@google.com>
Date:   Wed Aug 17 13:26:33 2016 -0700

    HIC: Improve AccelerationClassifier

    Apply a bunch of improvements to the acceleration classifier:

    - When dragging from the edges, we get some delay without
      movement between the DOWN and MOVE, which confounds the
      classifier. Now discounts data segments where the delay
      does not match the expected 16ms.
    - The distance ratio did not compensate for differences
      in sampling. If it does it's equivalent to the speed
      ratio however. The distance ratio was removed and the
      impact of the speed ratio score doubled.
    - If we cannot calculate the ratio, no longer penalize
      the traces for this.

    Bug: 27405075
    Change-Id: I067eb4d478593afbb20354e5c85a05353e2b4184