commit 70d7a06c705a481552617c2a551fd38e70044b1a
Author: Dries Buytaert <dries@buytaert.net>
Date:   Thu May 25 20:35:18 2000 +0000

    * Replaced the RDF support (rdf.php) with a newer version (backend.php).
      The new version is a generic framework that has everything ready to add
      support RSS and XML backends in a 100% transparant way.  It's a flexible
      framework. Other changes include: better coding, improved robustness and
      readability.
    * RSS and XML support will be integrated in near future.
    * Cache-invalidation is set to 30 minutes.