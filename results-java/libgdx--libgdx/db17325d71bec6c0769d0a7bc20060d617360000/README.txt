commit db17325d71bec6c0769d0a7bc20060d617360000
Author: nathan.sweet <nathan.sweet@6c4fd544-2939-11df-bb46-9574ba5d0bfa>
Date:   Mon Aug 29 17:59:39 2011 +0000

    Massive checkin...
    [changed] Skin format from XML to JSON so we get automated and read/write serialization.
    [added] No arg constructors to NinePatch and all skin styles.
    [updated] BitmapFont, added access to image file.
    [updated] BitmapFont "uses integer", mostly because I like changing things.
    [updated] scene2d, better defined semantics of touchUp, touchDown, and touchDragged. touchUp returning true means the actor will receive focus.
    [updated] Group, added transform boolean so we can have axis aligned groups that don't cause a batch flush.
    [deleted] Old actor classes.
    [updated] Rewrote FlickScrollPane.
    [added] Label methods.
    [updated] ScissorStack, prevent from scissoring zero size.
    [updated] JsonWriter, separated setting the name from methods that start an object and array. Support for multiple JSON-like output formats.
    [updated] Json, completely refactored, more and better thought out methods.
    [updated] JsonReader, reads non-JSON, JSON-like formats.
    [updated] Some tests that have been broken over time.
    [added] GestureDetector.