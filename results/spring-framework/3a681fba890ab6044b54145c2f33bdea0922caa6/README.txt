commit 3a681fba890ab6044b54145c2f33bdea0922caa6
Author: Arjen Poutsma <apoutsma@pivotal.io>
Date:   Tue Jun 28 11:17:57 2016 +0200

    AbstractResponseBodySubscriber improvements

     - AbstractResponseBodySubscriber now checks if the current state is
       expected before changing to a new state.
     - Included comments by @violetagg