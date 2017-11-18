commit 8068618ebed021c445db0919843cdb6b9b9e35b5
Author: Jesse Wilson <jessewilson@google.com>
Date:   Fri Jan 21 17:12:43 2011 -0800

    Update preloaded-classes for Honeycomb.

    We preload classes for two reasons. Classes that are popular can be
    shared and can increase the number of apps that can be run concurrently.
    Classes that initialize slowly can be initialized at system boot time
    by the zygote, decreasing the time to launch a specific app.

    To select which classes to preload, I exercised Android's built-in apps
    as well as these apps from Market: ESPN score center, Amazon, Flixster,
    Twitter, Adobe Reader, Ebay Mobile, Facebook, Solitare (Ken Magic),
    Barcode Reader, Google Earth and Square.

    A cycle of launching ~460 (non unique) activities in sequence took 9m35s
    with the previous preloaded-classes list. The update improves the launch
    time of the same sequence to 9m27s: the marginal improvement over the
    previous set of preloaded classes is negligible.

    http://b/3004763

    Change-Id: Ida511ae31eeff6d95d9cb6aacae68b9bb9dd2ebe