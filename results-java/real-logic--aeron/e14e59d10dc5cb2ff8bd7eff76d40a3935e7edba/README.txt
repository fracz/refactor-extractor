commit e14e59d10dc5cb2ff8bd7eff76d40a3935e7edba
Author: Todd L. Montgomery <tmont@nard.net>
Date:   Sun Jul 5 10:50:56 2015 -0700

    [Java]: reworked RetransmitHandler & LossDetector to remove usage of TimerWheel and replace with duty cycle hooks. Removed TimerWheel from DriverConductor. Should address #100 and improve latency and throughput.