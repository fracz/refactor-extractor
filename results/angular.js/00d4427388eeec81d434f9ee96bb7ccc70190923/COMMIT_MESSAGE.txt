commit 00d4427388eeec81d434f9ee96bb7ccc70190923
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Thu Mar 8 15:59:32 2012 -0800

    refactor($provide) Rename service -> provider

    It registers a provider class, so this makes more sense.

    Breaks Rename $provide.service -> $provide.provider