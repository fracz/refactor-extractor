commit d969ebad0794015133d37c5bb32fe9168c7e3c3b
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Mon May 8 09:57:54 2017 -0700

    Polish ConfigurationPropertySource support

    Improve ConfigurationPropertySource support by reworking some of the
    stream calls based on advice offered by Tagir Valeev from JetBrains.

    Also improved ConfigurationPropertySource.containsDescendantOf so that
    it returns an enum rather than an Optional<Boolean> (again based on
    feedback from Tagir).

    See gh-9000