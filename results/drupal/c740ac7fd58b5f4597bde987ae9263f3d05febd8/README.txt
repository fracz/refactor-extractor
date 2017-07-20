commit c740ac7fd58b5f4597bde987ae9263f3d05febd8
Author: Gábor Hojtsy <gabor@hojtsy.hu>
Date:   Fri May 4 09:41:37 2007 +0000

    #127539: progressive operation support, refactoring update.php code to a generic batch API to support runnning operations in multiple HTTP requests
      - update.php is already on the batch API
      - node access rebuilding is in the works
      - automatic locale importing is in the works

     Thanks to Yves Chedemois (yched) for the good code quality, very wide awareness of issues related to batches,
     and the fantastic turnaround times. Hats off.