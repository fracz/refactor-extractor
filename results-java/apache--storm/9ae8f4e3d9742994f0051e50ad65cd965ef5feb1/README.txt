commit 9ae8f4e3d9742994f0051e50ad65cd965ef5feb1
Author: Jungtaek Lim <kabhwan@gmail.com>
Date:   Thu Oct 13 19:00:10 2016 +0900

    STORM-1446 [Storm SQL] Compile the Calcite logical plan to Storm Trident logical plan

    * port SamzaSQL implementation to Storm
      * https://github.com/milinda/samza-sql
    * apply some rules to optimize
    * apply the improvement of STORM-2072
    * apply STORM-2200: Drop Aggregate & Join support on Trident mode
      * also documentation
    * optimize Calc
      * merge filter and projection scripts into one
      * also applying short circuit
    * modify Trident unit tests to use new query planner
    * arrange some files
      * Move some files which are only used from standalone
      * Remove some files which are no longer used
    * guard the possibility of stack overflow error on explaining
      * just leave error logs, and print out empty plan and continue
      * reported this behavior to Calcite community
    * leave some comments to clarify what it means