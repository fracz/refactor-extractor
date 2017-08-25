commit 8e7591654ada4233c579d859b7a34289297c1b7d
Author: Petr Škoda <commits@skodak.org>
Date:   Sat Jan 5 15:15:11 2013 +0100

    MDL-35356 improve worksheet exports

    This patch removes deprecated PEAR excel export lib in favour of newer PHPExcel,
    the ODS exporter implementation is finished and a test script is included.

    The default Excel format is now 2007, file extension is changed automatically
    to match excel format.