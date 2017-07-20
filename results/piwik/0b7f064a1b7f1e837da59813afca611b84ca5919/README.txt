commit 0b7f064a1b7f1e837da59813afca611b84ca5919
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Fri Jan 18 06:28:19 2008 +0000

    - RSS feed works!!
    - not using XMLSErializer anymore. generating XML a la mano
    - lots of small fixes / improvements
    - optimization on the archiving process
    - cleaned Renderers and fully testing the output
    - now handling date=previous10 and date=last15 for example
    - last10 works for days/week/month/year
    - fixed bug when adding a user
    - completed visitsSummary API
    -

    git-svn-id: http://dev.piwik.org/svn/trunk@183 59fd770c-687e-43c8-a1e3-f5a4ff64c105