commit 076de383f018f08aea7fc18b1710ce18cb794aa8
Author: rvesse <rvesse@dotnetrdf.org>
Date:   Thu Jan 29 17:04:09 2015 -0800

    Initial work on Under File System refactoring (TACHYON-221)

    - Moves Under File System implementations into their own separate
      modules
    - Adds a factory interface and a ServiceLoader driven registry for these
      factories
    - Refactor relevant code to go via this registry
    - Add connectFromMaster() and connectFromWorker() methods to
      UnderFileSystem to avoid hard coding login logic in the TachyonMaster
      and TachyonWorker

    As of this commit things will almost certainly not build successfully,
    this commit just does the basic restructuring and refactoring.

    Note that since doing any meaningful testing requires at least a local under file
    system client this remains in the core module