commit 055b6e4d6e6d99f13dddce6b63c408752cd8898b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Jan 13 14:58:17 2011 +0100

    made a big refactoring of the templating sub-framework

     * better separation of concerns
     * made TwigBundle independant of the PHP Engine from FrameworkBundle (WIP)
     * removed one layer of abstraction in the Templating component (renderers)
     * made it easier to create a new Engine for any templating library
     * made engines lazy-loaded (PHP engine for instance is not started if you only use Twig)
     * reduces memory footprint (if you only use one engine)
     * reduces size of compiled classes.php cache file