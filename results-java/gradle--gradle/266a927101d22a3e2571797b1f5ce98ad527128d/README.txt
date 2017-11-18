commit 266a927101d22a3e2571797b1f5ce98ad527128d
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Mar 25 12:59:19 2011 +0100

    In order to allow tooling api to run only single task & build the model for eclipse I did some simple refactorings in the eclipse/idea plugins. The stuff may be changed anyway when me & Adam attack on the major refactoring of GeneratorTask. Details:

    - removed generator task configurer
    - made the EclipseConfigurer / IdeaConfigurer responsible for building the model
    - updated tests accordingly