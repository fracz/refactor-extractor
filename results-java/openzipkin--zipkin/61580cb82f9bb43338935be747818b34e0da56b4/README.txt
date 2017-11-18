commit 61580cb82f9bb43338935be747818b34e0da56b4
Author: Adrian Cole <adriancole@users.noreply.github.com>
Date:   Sat Sep 24 13:51:34 2016 +0800

    Fixes thread safety issues and improves health checks on elasticsearch (#1311)

    This fixes a thread-safety concern raised by @sethp-jive on AWS signing.

    It also improves health check by not wrapping exceptions. This will
    allow users to troubleshoot the system easier.

    Finally, this throws on misconfigured credentials vs swallowing the
    exception. This will help people use health checks also, as it makes
    this configuration error visible without enabling debug logging.