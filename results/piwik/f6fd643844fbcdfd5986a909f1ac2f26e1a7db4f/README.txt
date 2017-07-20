commit f6fd643844fbcdfd5986a909f1ac2f26e1a7db4f
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Wed Dec 10 15:16:45 2014 +1300

    #6622 Logger refactoring: moved the container creation into a new ContainerFactory

    This class can be used in tests to create a specific environment (e.g. the prod environment, CLI environment, etc...)