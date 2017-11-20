commit a9b1e5d67421ed98b49ae25c3bbd6e27a0ab1590
Author: Bernd Pfrommer <bernd.pfrommer@gmail.com>
Date:   Mon Jun 22 15:45:59 2015 -0400

    bug fixes to insteonplm binding:
    - retry modem database download if it fails
    - much improved handling of corrupt data packets as received from the
      InsteonHub raw port
    - discard spurious messages for the insteon thermostat to avoid
      outlier temperature and humidity values