commit 0b086a9625cd2e8219711e04579466a43d4a4e99
Author: Jason Tedor <jason@tedor.me>
Date:   Wed Aug 3 13:43:51 2016 -0400

    Do not log on explicit no unsafe

    Motivation:

    When Netty components are initialized, Netty attempts to determine if it
    has access to unsafe. If Netty is not able to access unsafe (because of
    security permissions, or because the JVM was started with an explicit
    flag to tell Netty to not look for unsafe), Netty logs an info-level
    message that looks like a warning:

    Your platform does not provide complete low-level API for accessing
    direct buffers reliably. Unless explicitly requested, heap buffer will
    always be preferred to avoid potential system unstability.

    This log message can appear in applications that depend on Netty for
    networking, and this log message can be scary to end-users of such
    platforms. This log message should not be emitted if the application was
    started with an explicit flag telling Netty to not look for unsafe.

    Modifications:

    This commit refactors the unsafe detection logic to expose whether or
    not the JVM was started with a flag telling Netty to not look for
    unsafe. With this exposed information, the log message on unsafe being
    unavailable can be modified to not be emitted when Netty is explicitly
    told to not look for unsafe.

    Result:

    No log message is produced when unsafe is unavailable because Netty was
    told to not look for it.