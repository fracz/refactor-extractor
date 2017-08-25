commit 61ba0c5f30d9ab9726b0025a792e01d8822f38a4
Author: Zachary Tong <zacharyjtong@gmail.com>
Date:   Tue Dec 15 11:59:09 2015 -0500

    Fix bug where ping() and sniff() encounter NPE

    Because ping and sniff are invoked from the connectionpool, there is no $transport object available,
    which means subsequent performRequests() will NPE.

    This fix will only invoke the retry mechanism if $transport is not null, which *should* only occur
    in the case of ping/sniff (when we don't want to retry anyway).

    However, this is a bit dodgy approach, and should be fixed through an architectural change.  Transport
    is too insidious and needs to be refactored, since everything needs reference to it.  This will be a bigger
    change and break bwc though, so we'll deal with it for 3.0

    Related to #349