commit e4a636a8851af4330a0ebafbf642166c2e8cb5d7
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Mar 21 10:12:01 2011 +0100

    [FrameworkBundle] refactored the cache:clear command

    * removed the hack on the Kernel
    * removed inheritance from the warmup command
    * major cleanup