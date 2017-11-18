commit f994677626a6babc5121647646f17a7de85065a7
Author: Edman Anjos <edmanp@google.com>
Date:   Mon Nov 28 16:35:15 2016 +0100

    Refactor DPMS Cert Installer and App Restrictions delegation.

    The DevicePolicyManagerService currently supports delegation of
    certificate installation and application restriction management, both
    of which are individually handled by DPMS.

    Upcoming framework features will add four more delegation types,
    namely: block uninstall; app permission management; app access
    management; and system app enabler. At this moment it makes sense to
    refactor the underlying delegation system in DPMS so that current and
    future delegates can be handled in a more generic way.

    Bug: 33099995
    Test: DPMS unit tests
    Change-Id: I9e350143572c6690febdd59d1ed5149af8ee4388