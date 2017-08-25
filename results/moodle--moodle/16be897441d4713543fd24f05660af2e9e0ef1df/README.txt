commit 16be897441d4713543fd24f05660af2e9e0ef1df
Author: David Mudrak <david.mudrak@gmail.com>
Date:   Sat Mar 20 22:15:54 2010 +0000

    MDL-21652 html_table rendering refactored

    * class html_component does not exist any more
    * class html_table rendered via html_writer::table()
    * html_table, html_table_row and html_table_cell have public $attributes property to set their CSS classes
    * dropped rotateheaders feature, should be added again after more research of possible ways (<svg> is not nice IMHO)
    * dropped possibility to define CSS classes for table heading, body and footer - can be easily done and better done using just table class and context