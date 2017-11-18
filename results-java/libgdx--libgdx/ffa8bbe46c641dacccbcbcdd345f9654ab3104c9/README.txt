commit ffa8bbe46c641dacccbcbcdd345f9654ab3104c9
Author: nathan.sweet <nathan.sweet@6c4fd544-2939-11df-bb46-9574ba5d0bfa>
Date:   Mon Oct 31 06:39:55 2011 +0000

    [updated] scene.ui, refactoring and javadoc. Some minor API updates for sanity and consistency. Made all fields private and exposed them where appropriate.
    [updated] Scaling, renamed fit to fill and fill to fit. This will cause interesting and fun to track down bugs for all, I'm really sorry but I had the names backwards!
    [renamed] Cullable, moved to out of scene2d.ui since it is useful in group.
    [renamed] ComboBox to SelectBox, this bothered me forever. Better to rename it now than to add an actual combo box in the future.
    [updated] ScrollPane, refactored to more closely match FlickScrollPane.
    [updated] SplitPane, refactored layout.
    TextField, Widget, WidgetGroup, and Window have mostly escaped refactoring because I'm fucking tired of doing it at the moment!