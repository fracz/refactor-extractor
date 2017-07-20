commit fae319e77abf6ca7ab7a97e7bd0da0966db52888
Author: Bernhard Schussek <bschussek@gmail.com>
Date:   Sat Mar 26 16:03:09 2011 +0100

    [Form] Renderers are now created explicitely using FormFactory::createRenderer(). This improves performance on requests where a form does not need to be rendered