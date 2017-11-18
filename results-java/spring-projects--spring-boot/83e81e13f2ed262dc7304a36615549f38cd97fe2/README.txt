commit 83e81e13f2ed262dc7304a36615549f38cd97fe2
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Fri Jul 7 15:51:44 2017 +0100

    Add support of metadata generation for Endpoints

    This commit improves the configuration metadata annotation processor to
    explicitly handle `@Endpoint` annotated class. Adding a new endpoint on
    a project potentially creates the following keys:

    * `endpoints.<id>.enabled`
    * `endpoints.<id>.cache.time-to-live`
    * `endpoints.<id>.jmx.enabled`
    * `endpoints.<id>.web.enabled`

    Default values are extracted from the annotation type. If an endpoint
    is restricted to a given tech, properties from unrelated techs are not
    generated.

    Closes gh-9692