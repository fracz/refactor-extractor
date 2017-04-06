commit b6bdb450e3b849c8adfc6732c16c0c7de84a8fb0
Author: Benjamin Eberlei <kontakt@beberlei.de>
Date:   Fri Jan 18 14:08:59 2013 +0100

    Completly refactor the Serializer Options Pull Request to push context information directly and avoid state and dependencies between SerializerInterface and encoders/normalizers.