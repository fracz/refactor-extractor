commit bd57c1b46792b47d1319a9da7c71daa7cfdbc32f
Author: Anthony Green <ajgreen@cantab.net>
Date:   Tue Dec 10 09:01:41 2013 +0000

    Fix reading of Inputs
      - Background thread wasn't starting

    Minor improvement to start-up ordering, where there may have been a potential bug preventing the use of a boardNum other than zero.