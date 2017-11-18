commit 6723c63411aa7b7595ae7fe68daafbc252ff19a9
Author: Alistair Jones <alistair.jones@gmail.com>
Date:   Mon Sep 7 16:34:16 2015 +0100

    Restructure token handling to allow for other implementations.

    TokenHolder is now an interface. The previous implementation is renamed
    to DelegatingToken holder, because it delegates its caching and creation
    features to InMemoryTokenCache and TokenCreator respectively.

    Handling of the RelationshipTypeToken special cases has been improved by
    introducing TokenFactory, which encapsulates usage of the Token sub-type
    constructors.

    Usage of a TOKEN type parameter has been pushed right down to
    TokenStore to avoid casting, and generally make the typing more solid.

    Some Token arrays have been replaced with lists to make typing easier.
    In all cases these were converted from lists anyway so there is
    no additional allocation overhead.