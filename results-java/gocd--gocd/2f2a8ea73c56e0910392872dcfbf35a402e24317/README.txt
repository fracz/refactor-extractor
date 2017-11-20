commit 2f2a8ea73c56e0910392872dcfbf35a402e24317
Author: Ketan Padegaonkar <KetanPadegaonkar@gmail.com>
Date:   Tue Feb 9 11:52:52 2016 +0530

    Add system properties to disable gadget and gadget rendering server (#1908)

    The gadget rendering server is completely disabled at boot-time to
    improve server startup time. Links are hidden in all views where links
    are being rendered