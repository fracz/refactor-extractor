commit 98c0d38a440e91adeb0ac12928174046596cd8e1
Merge: 6a28718 77e2fa5
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Sep 25 08:05:47 2013 +0200

    merged branch jakzal/domcrawler-namespace-autodiscovery (PR #6650)

    This PR was merged into the master branch.

    Discussion
    ----------

    [DomCrawler] Added auto-discovery and explicit registration of namespaces in filter() and filterByXPath()

    | Q | A
    | --- | ---
    |Bug fix: | no
    |Feature addition: |yes
    |Backwards compatibility break: | yes, default namespace is no longer removed in the `addContent` method
    |Symfony2 tests pass: | yes|
    |Fixes the following tickets: | #4845
    |Todo: | -
    |License of the code:| MIT
    |Documentation PR: | symfony/symfony-docs#2979

    * added support for automatic discovery and explicit registration of document namespaces for `Crawler::filterXPath()` and `Crawler::filter()`
    * improved content type guessing in `Crawler::addContent()`
    * [BC BREAK] `Crawler::addXmlContent()` no longer removes the default document namespace

    I mentioned in #4845 it would probably be possible to use [DOMNode::lookupNamespaceURI()](http://www.php.net/manual/en/domnode.lookupnamespaceuri.php) to find a namespace URI by given prefix. Unfortunately we cannot use it here since we'd have to call it on a node in the namespace we're looking for.

    Current implementation makes the following query to find a namespace:
    ```php
    $domxpath->query('(//namespace::*[name()="media"])[last()]')
    ```

    Commits
    -------

    77e2fa5 [DomCrawler] Removed checks if CssSelector is present.
    9110468 [DomCrawler] Enabled manual namespace registration.
    be1e4e6 [DomCrawler] Enabled default namespace prefix overloading.
    943d446 [DomCrawler] Updated the CHANGELOG with namespace auto-registration details.
    c6fbb13 [DomCrawler] Added support for an automatic default namespace registration.
    587e2dd [DomCrawler] Made that default namespace is no longer removed when loading documents with addXmlContent().
    c905bba [DomCrawler] Added more tests for namespaced filtering.
    6e717a3 [DomCrawler] Made sure only the default namespace is removed when loading an XML content.
    e5b8abb [DomCrawler] Added auto-discovery of namespaces in Crawler::filter() and Crawler::filterByXPath().