commit 1f0d45d79552a09210328b34c984326387d983de
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Mon Feb 23 18:28:23 2015 -0800

    Protect against NPE and improve error message

    Update ConfigurationMetadataAnnotationProcessor so that `prefix` is
    only obtained when the annotation is not null. Also improve exception
    message by including the element.