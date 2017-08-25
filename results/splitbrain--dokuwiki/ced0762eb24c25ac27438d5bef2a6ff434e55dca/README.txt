commit ced0762eb24c25ac27438d5bef2a6ff434e55dca
Author: chris <chris@jalakai.co.uk>
Date:   Sun Aug 27 01:43:33 2006 +0200

    ft_snippet() update

     - correct "opt1" algorithm for multibyte utf8
     - minor improvement to "opt2" for short pages
     - add "utf8" algorithm, this algorithm endeavours
       to work with whole utf8 character as much as
       possible.  The resulting snippet will tend to
       100 characters, rather than the 100 bytes of
       "opt1" and "opt2".

    darcs-hash:20060826234333-9b6ab-ae4c60c8855a92b133cb8d5a230098203f610e7b.gz