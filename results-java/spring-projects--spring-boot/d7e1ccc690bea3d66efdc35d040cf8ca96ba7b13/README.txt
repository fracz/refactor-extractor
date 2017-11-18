commit d7e1ccc690bea3d66efdc35d040cf8ca96ba7b13
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Wed Jul 27 18:12:29 2016 -0700

    Improve configuration properties logging

    Update ConfigurationPropertiesBindingPostProcessor with improved logging
    when multiple PropertySourcesPlaceholderConfigurer beans are found.

    See gh-6457