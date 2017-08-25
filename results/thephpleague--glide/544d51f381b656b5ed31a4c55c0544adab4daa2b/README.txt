commit 544d51f381b656b5ed31a4c55c0544adab4daa2b
Author: Jonathan Reinink <jonathan@reinink.ca>
Date:   Mon Jun 15 19:37:13 2015 -0400

    Started (yet another) of large refactor.
    Removed ApiFactory in favour of just using ServerFactory.
    Moved all manipulators to their own namespace.
    Added new Watermark manipulator.
    Merged RequestArgumentsResolver into RequestFactory.
    Simplified RequestFactory.
    Added new ResponseFactoryInterface.
    Set StreamedResponseFactory as default response factory.
    Moved all URL builder classes to new Urls namespace.
    Moved all HTTP signature classes to new Signatures namespace.
    Added the ability to get Base64 encoded images.
    Changed caching to organize manipulated images into folders.
    Added method to delete cache images.
    Removed HTTP Signature value when generating cache filenames.
    Added support for progressive JPEGs.
    Fixed bug when setting non-string manipulation parameters.