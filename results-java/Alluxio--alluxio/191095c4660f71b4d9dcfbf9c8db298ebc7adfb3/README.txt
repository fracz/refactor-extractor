commit 191095c4660f71b4d9dcfbf9c8db298ebc7adfb3
Author: Manu Goyal <manu.goyal2013@gmail.com>
Date:   Fri Sep 25 17:53:29 2015 -0700

    [TACHYON-1061] Throw detailed exceptions isolated from RPC framework

    The bulk of this commit is renaming/refactors, so below we detail
    the important things to look at

    In tachyon.thrift, we deleted all the thrift exceptions, and
    replaced them with two exceptions, TachyonTException and
    ThriftIOException, which are general containers for Tachyon
    exceptions and IOExceptions, respectively. Also modified the
    service interfaces to only throw those exceptions.

    In tacyhon/exception/, we defined the TachyonException class as a
    superclass of all Tachyon exceptions, which can convert back and
    forth between TachyonTException using error codes. We then
    defined an exception subclass for each of the error code values,
    which will be the specific exceptions that we throw around in
    Tachyon everywhere except the thrift layer.

    Finally, in the client code, whenever we make an RPC call, we
    immediately convert the TachyonTException to a TachyonException
    and the ThriftIOException to an IOException, and propagate that
    up to the top layer of the client. At any user-facing boundary of
    the client (like TachyonFileSystem), we'll unwrap the
    TachyonException with any specific ones we think it might throw.
    Otherwise, we'll throw the general TachyonException as-is, as a
    catch-all for exceptions we didn't expect, or might add later. By
    including the TachyonException in all the 'throws' clauses of the
    function signatures, we allow for backwards compatibility by
    ensuring that newly added exceptions won't cause a compile-time
    error in user code.