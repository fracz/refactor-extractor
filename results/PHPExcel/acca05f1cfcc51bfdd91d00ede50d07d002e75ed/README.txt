commit acca05f1cfcc51bfdd91d00ede50d07d002e75ed
Author: Dominik Bonsch <dominik.bonsch@webfrap.net>
Date:   Tue Jan 22 20:39:45 2013 +0100

    minor architecture improvement
      - used strtoupper to get rid of case sensitive name checks
      - added "else if", it's not nessecary to check for the second "if", if
    the first one was allready true