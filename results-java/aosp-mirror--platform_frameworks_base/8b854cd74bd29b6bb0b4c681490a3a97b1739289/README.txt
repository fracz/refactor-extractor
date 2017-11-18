commit 8b854cd74bd29b6bb0b4c681490a3a97b1739289
Author: Jack Palevich <jackpal@google.com>
Date:   Mon Mar 19 11:17:40 2012 -0700

    Remove EGL context limit for Adreno GPUs.

    This change allows Adreno GPUs to have multiple EGL contexts. We had
    to limit this in earlier versions of Android due to limitations in
    the Adreno GPU driver (only 8 EGL contexts allowed system wide.)
    That brand of GPU has improved its EGL drivers to support multiple
    EGL contexts in more recent versions of their drivers used on more
    recent versions of Android.

    Bug: 6142005
    Change-Id: Id3030466be9a3d9fbe728f1785378c1f05da98fe