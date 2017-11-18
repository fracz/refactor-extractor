commit e2297bc8ab84fc4e52982ded4f020c81257f8acc
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Nov 4 11:59:00 2011 +0100

    Fixed an issue in our test infrastructure related to not passing correct test id for onOuput events. Previously, only the suite id was passed when the test triggered any output events. Now we're passing the correct test id. Now it is possible to improve the visual side of printing standard streams from tests. It is one of the acceptance criteria for the 'show standard streams for tests' story.