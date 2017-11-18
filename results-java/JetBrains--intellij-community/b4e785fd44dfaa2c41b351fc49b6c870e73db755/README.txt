commit b4e785fd44dfaa2c41b351fc49b6c870e73db755
Author: Julia Beliaeva <Julia.Beliaeva@jetbrains.com>
Date:   Thu Dec 22 22:20:12 2016 +0300

    [vcs-log] refactor VcsLogUi and VcsLogUiProperties relationships

    Before that, to add a new configuration settings, one had to add at least 8 methods: to VcsLogUi and implementation, and to VcsLogUiProperties and implementation, set and get methods to each of this classes. Moreover, this was not especially helpful to the new file history implementation. VcsLogUi interface was huge and its file-history-implementation had a lot of methods that did nothing. Also, file history requires shared properties, ie one properties instance for several tabs (it does not make sense to have settings specifically for each file path). Shared properties were hard to implement with VcsLogUi acting as a controller.

    So, what's done is:
    - listeners for VcsLogUiProperties are introduced, they are notified when properties change;
    - VcsLogUi does not have set/get-method pairs anymore (isHighlighterEnabled remained, however, needed for file-history);
    - actions get VcsLogUiProperties from context and change settings directly in it;
    - VcsLogUiImpl adds a listener and listens.