commit a1c9efd6a85a19b61487d99852629c951fc10457
Author: Alexander Garagatyi <agaragatyi@codenvy.com>
Date:   Wed Apr 26 12:36:51 2017 +0300

    CHE-4310: improve UX of usage of official docker images for machines

    Add tail -f /dev/null into containers that exits on start.
    Check if container is running after start of container.
    Signed-off-by: Alexander Garagatyi