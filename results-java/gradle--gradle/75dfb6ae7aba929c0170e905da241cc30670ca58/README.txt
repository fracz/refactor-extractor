commit 75dfb6ae7aba929c0170e905da241cc30670ca58
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Nov 8 18:35:38 2011 +0100

    Attempted to generalize the concept of parsing notations. The result can be seen in ForcedModuleParser, where a builder is used to assemble the parsing algorithm. The refactoring continues. Needs some reviewing soon, it might be a bit over-engineered.