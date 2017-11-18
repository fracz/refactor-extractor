commit f8ded6de633aee8cba00722d98f912eaf1bde22d
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Sat Jan 21 07:38:52 2017 -0800

    Don't use DataBinder to work out excludes

    Update `AutoConfigurationImportSelector` so that exclude properties
    are loaded without invoking a `DataBinder`. This optimization helps
    to improve application startup time.

    See gh-7573