commit cad8ca1889e7fb068034c91197b38e7cc8d841d2
Author: Henrik Nyman <henrik.nyman@neotechnology.com>
Date:   Thu Jul 14 20:10:42 2016 +0200

    Allow plugins that implements both separate auth interfaces

    A plugin that implements both AuthenticationPlugin and AuthorizationPlugin
    can be instantiated as one plugin realm instance (and not duplicated)

    - Some test refactoring