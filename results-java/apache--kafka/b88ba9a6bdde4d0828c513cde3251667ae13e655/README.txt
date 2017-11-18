commit b88ba9a6bdde4d0828c513cde3251667ae13e655
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Mon Oct 5 12:01:33 2015 -0700

    KAFKA-2604; Remove `completeAll` and improve timeout passed to `Selector.poll` from `NetworkClient.poll`

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Ewen Cheslack-Postava, Jason Gustafson, Guozhang Wang

    Closes #272 from ijuma/kafka-2640-remove-complete-all-poll-timeout