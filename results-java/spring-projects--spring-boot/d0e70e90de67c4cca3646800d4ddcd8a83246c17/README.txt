commit d0e70e90de67c4cca3646800d4ddcd8a83246c17
Author: Craig Andrews <candrews@integralblue.com>
Date:   Wed Jul 12 14:52:36 2017 -0400

    When pool autocommit is disabled, inform Hibernate

    Starting with Hibernate 5.2.10, the JPA property
    `hibernate.connection.provider_disables_autocommit` should be set to true
    when the datasource has autocommit disabled in order to improve
    performance.

    See gh-9737