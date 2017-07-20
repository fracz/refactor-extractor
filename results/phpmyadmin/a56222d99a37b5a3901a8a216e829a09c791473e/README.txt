commit a56222d99a37b5a3901a8a216e829a09c791473e
Author: Sebastian Mendel <cybot_tm@users.sourceforge.net>
Date:   Tue Mar 13 09:55:19 2007 +0000

    improved displaying of binary logs:
     - limit displayed rows to $cfg[MaxRows]
     - show single and summary log file size (MySQL >= 5.0.7)
    cleaned up the code
    added some more sanity checks
    source documentation