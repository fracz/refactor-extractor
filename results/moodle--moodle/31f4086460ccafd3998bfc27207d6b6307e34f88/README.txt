commit 31f4086460ccafd3998bfc27207d6b6307e34f88
Author: Thanh Le <d.t.le@open.ac.uk>
Date:   Tue Nov 20 10:07:06 2012 +0000

    MDL-36259 course short names: ensure it displays when enabled in admin

    Fixed 5 Moodle pages that does not display the course short name even if "Display extended course names" setting is on.  In fixing this, search.php also included related minor code to change to minimise DB calls to improve performance.