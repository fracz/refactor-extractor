commit d2678e08deab3c69d980fe80946e0fbd2c935f18
Author: Phillip Webb <pwebb@gopivotal.com>
Date:   Fri Nov 8 00:29:23 2013 -0800

    Improve startup performance for nested JARs

    Refactor spring-boot-loader to work directly with low level zip data
    structures, removing the need to read every byte when the application
    loads.

    This change was initially driven by the desire to improve tab-completion
    time when working with the Spring CLI tool. Local tests show CLI
    startup time improving from ~0.7 to ~0.22 seconds.

    Startup times for regular Spring Boot applications are also improved,
    for example, the tomcat sample application now starts 0.5 seconds
    faster.