commit 169c72406cf283b0bb8c7e6ec8ee17e6e0be4f8b
Author: chris <chris@jalakai.co.uk>
Date:   Tue Feb 6 02:54:54 2007 +0100

    refactor xhtml TOC creation into a class function, FS#1058

    - changed inc/parser/xhtml.php, render_TOC() function
      it now takes a toc array as a parameter
    - refactor render_TOC()  and _tocitem() into static class functions

    The xhtml renderer can build its TOC using $this->render_TOC($this->toc)
    Templates can create a separate TOC using
      echo Doku_Renderer_xhtml::render_TOC(p_get_metadata($ID, 'description
    tableofcontents'));

    darcs-hash:20070206015454-9b6ab-b3bd7ff772b756f8aaf496cb319eb73994cdbd94.gz