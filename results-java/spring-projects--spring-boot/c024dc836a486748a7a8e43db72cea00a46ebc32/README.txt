commit c024dc836a486748a7a8e43db72cea00a46ebc32
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Tue Jun 28 16:16:34 2016 +0200

    Support Spring Mobile for all template engines

    Previously, Spring Mobile was only supported for Thymeleaf and JSPs. This
    commit improves the auto-configuration to also provide device delegating
    support for Freemarker, Groovy Templates and Mustache.

    Closes gh-5140