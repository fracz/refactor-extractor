commit 403158b54b18cabf93eb15d4c4dd8ab66604bf9f
Author: Sriharsha Chintalapani <schintalapani@hortonworks.com>
Date:   Tue Oct 20 14:13:34 2015 -0700

    KAFKA-1686; Implement SASL/Kerberos

    This PR implements SASL/Kerberos which was originally submitted by harshach as https://github.com/apache/kafka/pull/191.

    I've been submitting PRs to Harsha's branch with fixes and improvements and he has integrated all, but the most recent one. I'm creating this PR so that the Jenkins can run the tests on the branch (they pass locally).

    Author: Ismael Juma <ismael@juma.me.uk>
    Author: Sriharsha Chintalapani <harsha@hortonworks.com>
    Author: Harsha <harshach@users.noreply.github.com>

    Reviewers: Ismael Juma <ismael@juma.me.uk>, Rajini Sivaram <rajinisivaram@googlemail.com>, Parth Brahmbhatt <brahmbhatt.parth@gmail.com>, Jun Rao <junrao@gmail.com>

    Closes #334 from ijuma/KAFKA-1686-V1