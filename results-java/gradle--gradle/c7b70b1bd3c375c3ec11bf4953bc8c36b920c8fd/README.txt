commit c7b70b1bd3c375c3ec11bf4953bc8c36b920c8fd
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Fri Feb 24 20:28:17 2012 +0100

    honor global Maven settings file when locating local Maven repository

    - renamed (Default)LocalMavenCacheLocator to (Default)LocalMavenRepositoryLocator and decoupled/improved the implementation
    - honor placeholders for system properties and environment variables in path for local Maven repository
    - renamed MavenSettingsProvider to (Default)MavenFileLocations