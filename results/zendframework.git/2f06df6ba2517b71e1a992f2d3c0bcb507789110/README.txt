commit 2f06df6ba2517b71e1a992f2d3c0bcb507789110
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Mon Sep 17 15:38:42 2012 -0500

    Refactored Sitemap helper to use Escaper

    - refactored xmlEscape() to use escapeHtml helper instead of
      htmlspecialchars()