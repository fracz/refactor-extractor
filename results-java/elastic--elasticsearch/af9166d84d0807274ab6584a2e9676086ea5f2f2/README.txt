commit af9166d84d0807274ab6584a2e9676086ea5f2f2
Author: Simon Willnauer <simonw@apache.org>
Date:   Thu Sep 17 10:26:31 2015 +0200

    Use a dedicated id to serialize EsExceptions instead of it's class name.

    Classnames change quickly due to refactorings etc. If that happens in a minor release
    we loose the ability to deserialize the exceptoin coming from another node sicne we today
    look it up by classname. This change uses a dedicated static id instead of the classname
    to lookup the actual class.