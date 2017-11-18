commit e058f76dabf3f805d1c220950a4f42c2ec605ecd
Author: Coda Hale <coda.hale@gmail.com>
Date:   Tue Jun 5 22:47:00 2012 -0700

    Drop contribs; promote ganglia, graphite, and web.

    This means dropping support for metrics-guice, metrics-spring, and metrics-sigar. These deserve to be their own projects with their own release schedules and committers. I'm willing to do what I can with metrics-core to help them improve, but me being responsible for these packages is a worst-case scenario.