commit b6b709cb5eb60e5c29f188dcd5faf3f4e770a198
Author: Vidit Drolia <vdrolia@yammer-inc.com>
Date:   Tue Mar 27 15:41:49 2012 -0700

    Update HttpClientConfiguration, Refactor HttpClientFactory

    HttpClientFactory:
    - refactor to allow sub-classes to override creating HTTP params and
      connection manager
    - add connection reuse strategy adn keep alive strategy based on
      keepAlive configuration
    - remove the use of 'timeout' for 'connectionTimeout' (use the new
      config value instead)

    HttpClientFactory:
    - add connectionTimeout, maxConnectionsPerRoute, keepAlive

    Note: keepAlive default value is 0 - NoConnectionReuseStrategy