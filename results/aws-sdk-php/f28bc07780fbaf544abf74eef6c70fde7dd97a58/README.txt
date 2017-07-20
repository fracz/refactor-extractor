commit f28bc07780fbaf544abf74eef6c70fde7dd97a58
Author: Michael Dowling <mtdowling@gmail.com>
Date:   Mon Feb 2 14:14:43 2015 -0800

    Adding signature providers.

    This commit updates clients to no longer provide a getSignature()
    method, but rather utilizes an internal signature provider, that
    provided a version, service, and region returns a SignatureInterface
    object used to sign requests. This allows customers to inject custom
    signature providers, allows customers to provide a signature version
    override to use when signing requests (e.g., for S3, use v4 instead),
    and allows clients to vary the signature version on a per/operation
    basis (e.g., this is already a possibility as some operations are not
    signed while others are signed).

    This commit also updates the SDK to use a few recent Guzzle Command
    changes: client options are now immutable, so some of the setOption
    calls have been refactored to use a new "config" client factory
    option type, which means that the option will be passed through to
    the getConfig() array of a client. For example, "signature_version"
    is now always available in the getConfig() array and is the
    preferred signature version of the client. This information is used
    in the SDK in places like the Multipart upload abstraction to know
    which type of hash to add to an upload.