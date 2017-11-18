commit ad318b0fa108886a6a30cca9df2bdd4fb7fcb396
Author: Angel Misevski <misevskia@gmail.com>
Date:   Fri Feb 10 19:10:29 2017 -0500

    Refactor inspectContainer() and add commit(), removeImage() (#4085)

    Adds commit() and removeImage() implementations to
    OpenShiftConnector. This requires some refactoring of existing
    methods (mostly refactoring repeated processes into their own
    methods -- e.g. creating an ImageStreamTag and getting image
    info from a tag).

    Additionally, refactors inspectContainer() method to remove
    a call to DockerConnector, instead obtaining the same information
    from what's available through the OpenShift API. This fixes an
    issue where the IP address of a workspace was unavailable from
    the ContainerInfo returned by DockerConnector.

    Signed-off-by: Angel Misevski <amisevsk@redhat.com>