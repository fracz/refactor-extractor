commit baea17b53fbdb7eae92607ed18f3d18110849712
Author: Yannick Welsch <yannick@welsch.lu>
Date:   Fri Dec 23 12:23:52 2016 +0100

    Separate cluster update tasks that are published from those that are not (#21912)

    This commit factors out the cluster state update tasks that are published (ClusterStateUpdateTask) from those that are not (LocalClusterUpdateTask), serving as a basis for future refactorings to separate the publishing mechanism out of ClusterService.