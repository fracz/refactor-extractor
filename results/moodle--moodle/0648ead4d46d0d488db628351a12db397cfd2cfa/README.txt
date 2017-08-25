commit 0648ead4d46d0d488db628351a12db397cfd2cfa
Author: David Mudrak <david.mudrak@gmail.com>
Date:   Mon Mar 22 08:16:30 2010 +0000

    MDL-21652 removed html_table property 'style'

    I copied this initially from the legacy html_component during the refactoring and forgot to check
    if it is actually used. Credit goes to Sam H. for spotting it.