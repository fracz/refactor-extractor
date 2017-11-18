commit 2a1810c4f7184df51ef64babcc45020beaaeb5a7
Author: Heribert Hirth <git@frostcode.de>
Date:   Wed Aug 27 01:17:31 2014 +0200

    Added parameter checks to NetClientImpl, CaOptionsImpl, FileSystemImpl, HttpClientImpl, HttpClientRequestImpl, SocketAddressImpl, WebSocketConnectOptionsImpl, added helper class Arguments for argument validation analogous to Objects.requireNonNull(), refactored parameter checks to use consistently Objects.requireNonNull(), added unit tests for several existing parameter checks, fixed small typo in CONTRIBUTING.md.

    Signed-off-by: Heribert Hirth <git@frostcode.de>