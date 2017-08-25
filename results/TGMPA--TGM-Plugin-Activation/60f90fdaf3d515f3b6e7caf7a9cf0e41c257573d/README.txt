commit 60f90fdaf3d515f3b6e7caf7a9cf0e41c257573d
Author: Gary Jones <gary@garyjones.co.uk>
Date:   Tue Oct 18 08:17:25 2011 +0100

    Fixes two credentials issues.

    One with nonces when not FTP details are not defined in wp-config.php (fixes #45).

    Another unreported bug which improves how a failed FTP connection would otherwise respond without showing an error message.