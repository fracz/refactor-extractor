commit 7c2ba554eb62fbdf2675d6af2f8cbb301ed63321
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Sep 14 17:43:16 2012 +0200

    Bootstrap plugin improvements (maven2Gradle). Next step of using embedded maven instead of forking off a separate maven process.

    1. Instead of a separate process that runs 'mvn help:effectivePom', now we're using maven3 classes to build the maven model. It's ~20 more jars and most of them need repackaging so that they don't clash with stuff inside maven-ant-tasks.
    2. Decided to add a missing capability to jarjar so that it correctly uses repackaged fully qualified class names in the resources. I needed that because maven3 classes are referred in the META-INF/plexus/components.xml resources (configuration of the plexus IoC container). I've forked jarjar to my github account, made the change, built the jar and uploaded it to repo.gradle.org.
    3. Maven2Gradle utility class (it was ported from the original open source project) still have the same interface: takes the effective pom xml content on input and prepares build.gradle / settings.gradle files as output. To make that possible I'm serializing the maven object model into xml via Maven3 classes (see MavenProjectXmlWriter).
    4. Some tidy-up and changes are still pending so it's not yet completed.