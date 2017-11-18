commit 195c438883dfcca799aa456dfe52d24c077e40da
Author: Daniel Sandler <dsandler@android.com>
Date:   Mon Nov 24 18:54:40 2014 -0500

    Fixing bugs in the LLand.

          less garish hue on the bugdroid
           :
      +----:-----------+
      | |  : |    Θ.......... improved pop styles
      | |  : @    |    |
      | O  A      |....... occasionally there was no gap to fly
      |      0    O    |   through, creating an unintentional
      | @    |    |    |   commentary on the futility of it all
      +----------------+
         :      :
         :      : animation could continue
         :        after activity pause, sapping
         :        precious cycles
         :
       failure is now more visceral

    Bug: 17931806
    Change-Id: Iea9bd88e340beb3a0ca310db071ec9f6ec719a33