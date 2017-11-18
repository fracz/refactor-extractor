/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.fs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Random;

import org.junit.*;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileContext.CreateOpts;
import org.apache.hadoop.fs.permission.FsPermission;

/**
 * <p>
 * A collection of tests for the {@link FileContext}.
 * This test should be used for testing an instance of FileContext
 *  that has been initialized to a specific default FileSystem such a
 *  LocalFileSystem, HDFS,S3, etc.
 * </p>
 * <p>
 * To test a given {@link FileSystem} implementation create a subclass of this
 * test and override {@link #setUp()} to initialize the <code>fc</code>
 * {@link FileContext} instance variable.
 *
 * Since this a junit 4 you can also do a single setup before
 * the start of any tests.
 * E.g.
 *     @BeforeClass   public static void clusterSetupAtBegining()
 *     @AfterClass    public static void ClusterShutdownAtEnd()
 * </p>
 */
public abstract class FileContextMainOperationsBaseTest  {


  private static String TEST_ROOT_DIR =
    System.getProperty("test.build.data", "/tmp");

  protected Path getTestRootPath(String pathString) {
    return fc.makeQualified(new Path(TEST_ROOT_DIR, pathString));
  }


  protected static FileContext fc;
  private static byte[] data = new byte[getBlockSize() * 2]; // two blocks of data
  {
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) (i % 10);
    }
  }

  @Before
  public void setUp() throws Exception {
    fc.mkdirs(getTestRootPath("test"), FileContext.DEFAULT_PERM);
  }

  @After
  public void tearDown() throws Exception {
    fc.delete(getTestRootPath("test"), true);
  }

  protected static int getBlockSize() {
    return 1024;
  }

  protected Path getDefaultWorkingDirectory() throws IOException {
    return getTestRootPath("/user/" + System.getProperty("user.name")).makeQualified(
              fc.getDefaultFileSystem().getUri(), fc.getWorkingDirectory());
  }

  protected boolean renameSupported() {
    return true;
  }

  @Test
  public void testFsStatus() throws Exception {
    FsStatus fsStatus = fc.getFsStatus(null);
    Assert.assertNotNull(fsStatus);
    //used, free and capacity are non-negative longs
    Assert.assertTrue(fsStatus.getUsed() >= 0);
    Assert.assertTrue(fsStatus.getRemaining() >= 0);
    Assert.assertTrue(fsStatus.getCapacity() >= 0);
  }

  @Test
  public void testWorkingDirectory() throws Exception {

    Path workDir = getDefaultWorkingDirectory();
    Assert.assertEquals(workDir, fc.getWorkingDirectory());

    fc.setWorkingDirectory(new Path("."));
    Assert.assertEquals(workDir, fc.getWorkingDirectory());

    fc.setWorkingDirectory(new Path(".."));
    Assert.assertEquals(workDir.getParent(), fc.getWorkingDirectory());

    // cd using a relative path
    Path relativeDir = new Path("existingDir1");
    Path absoluteDir = new Path(workDir.getParent(),"existingDir1");
    fc.mkdirs(absoluteDir, FileContext.DEFAULT_PERM);
    fc.setWorkingDirectory(relativeDir);
    Assert.assertEquals(absoluteDir,
                                        fc.getWorkingDirectory());
    // cd using a absolute path
    absoluteDir = getTestRootPath("test/existingDir2");
    fc.mkdirs(absoluteDir, FileContext.DEFAULT_PERM);
    fc.setWorkingDirectory(absoluteDir);
    Assert.assertEquals(absoluteDir, fc.getWorkingDirectory());

    // Now open a file relative to the wd we just set above.
    Path absolutePath = new Path(absoluteDir, "foo");
    fc.create(absolutePath, EnumSet.of(CreateFlag.CREATE)).close();
    fc.open(new Path("foo")).close();

    absoluteDir = getTestRootPath("nonexistingPath");
    try {
      fc.setWorkingDirectory(absoluteDir);
      Assert.fail("cd to non existing dir should have failed");
    } catch (Exception e) {
      // Exception as expected
    }

    // Try a URI
    absoluteDir = new Path("file:///tmp/existingDir");
    fc.mkdirs(absoluteDir, FileContext.DEFAULT_PERM);
    fc.setWorkingDirectory(absoluteDir);
    Assert.assertEquals(absoluteDir, fc.getWorkingDirectory());

  }

  @Test
  public void testMkdirs() throws Exception {
    Path testDir = getTestRootPath("test/hadoop");
    Assert.assertFalse(fc.exists(testDir));
    Assert.assertFalse(fc.isFile(testDir));

    Assert.assertTrue(fc.mkdirs(testDir, FsPermission.getDefault()));

    Assert.assertTrue(fc.exists(testDir));
    Assert.assertFalse(fc.isFile(testDir));

    Assert.assertTrue(fc.mkdirs(testDir, FsPermission.getDefault()));

    Assert.assertTrue(fc.exists(testDir));
    Assert.assertFalse(fc.isFile(testDir));

    Path parentDir = testDir.getParent();
    Assert.assertTrue(fc.exists(parentDir));
    Assert.assertFalse(fc.isFile(parentDir));

    Path grandparentDir = parentDir.getParent();
    Assert.assertTrue(fc.exists(grandparentDir));
    Assert.assertFalse(fc.isFile(grandparentDir));

  }

  @Test
  public void testMkdirsFailsForSubdirectoryOfExistingFile() throws Exception {
    Path testDir = getTestRootPath("test/hadoop");
    Assert.assertFalse(fc.exists(testDir));
    Assert.assertTrue(fc.mkdirs(testDir, FsPermission.getDefault()));
    Assert.assertTrue(fc.exists(testDir));

    createFile(getTestRootPath("test/hadoop/file"));

    Path testSubDir = getTestRootPath("test/hadoop/file/subdir");
    try {
      fc.mkdirs(testSubDir, FsPermission.getDefault());
      Assert.fail("Should throw IOException.");
    } catch (IOException e) {
      // expected
    }
    Assert.assertFalse(fc.exists(testSubDir));

    Path testDeepSubDir = getTestRootPath("test/hadoop/file/deep/sub/dir");
    try {
      fc.mkdirs(testDeepSubDir, FsPermission.getDefault());
      Assert.fail("Should throw IOException.");
    } catch (IOException e) {
      // expected
    }
    Assert.assertFalse(fc.exists(testDeepSubDir));

  }

  @Test
  public void testGetFileStatusThrowsExceptionForNonExistentFile()
    throws Exception {
    try {
      fc.getFileStatus(getTestRootPath("test/hadoop/file"));
      Assert.fail("Should throw FileNotFoundException");
    } catch (FileNotFoundException e) {
      // expected
    }
  }

  public void testListStatusThrowsExceptionForNonExistentFile()
                                                    throws Exception {
    try {
      fc.listStatus(getTestRootPath("test/hadoop/file"));
      Assert.fail("Should throw FileNotFoundException");
    } catch (FileNotFoundException fnfe) {
      // expected
    }
  }

  @Test
  public void testListStatus() throws Exception {
    Path[] testDirs = { getTestRootPath("test/hadoop/a"),
                        getTestRootPath("test/hadoop/b"),
                        getTestRootPath("test/hadoop/c/1"), };
    Assert.assertFalse(fc.exists(testDirs[0]));

    for (Path path : testDirs) {
      Assert.assertTrue(fc.mkdirs(path, FsPermission.getDefault()));
    }

    FileStatus[] paths = fc.listStatus(getTestRootPath("test"));
    Assert.assertEquals(1, paths.length);
    Assert.assertEquals(getTestRootPath("test/hadoop"), paths[0].getPath());

    paths = fc.listStatus(getTestRootPath("test/hadoop"));
    Assert.assertEquals(3, paths.length);


    Assert.assertTrue(getTestRootPath("test/hadoop/a").equals(paths[0].getPath()) ||
        getTestRootPath("test/hadoop/a").equals(paths[1].getPath()) ||
        getTestRootPath("test/hadoop/a").equals(paths[2].getPath()));
    Assert.assertTrue(getTestRootPath("test/hadoop/b").equals(paths[0].getPath()) ||
        getTestRootPath("test/hadoop/b").equals(paths[1].getPath()) ||
        getTestRootPath("test/hadoop/b").equals(paths[2].getPath()));
    Assert.assertTrue(getTestRootPath("test/hadoop/c").equals(paths[0].getPath()) ||
        getTestRootPath("test/hadoop/c").equals(paths[1].getPath()) ||
        getTestRootPath("test/hadoop/c").equals(paths[2].getPath()));


    paths = fc.listStatus(getTestRootPath("test/hadoop/a"));
    Assert.assertEquals(0, paths.length);
  }

  @Test
  public void testWriteReadAndDeleteEmptyFile() throws Exception {
    writeReadAndDelete(0);
  }

  @Test
  public void testWriteReadAndDeleteHalfABlock() throws Exception {
    writeReadAndDelete(getBlockSize() / 2);
  }

  @Test
  public void testWriteReadAndDeleteOneBlock() throws Exception {
    writeReadAndDelete(getBlockSize());
  }

  @Test
  public void testWriteReadAndDeleteOneAndAHalfBlocks() throws Exception {
    writeReadAndDelete(getBlockSize() + (getBlockSize() / 2));
  }

  @Test
  public void testWriteReadAndDeleteTwoBlocks() throws Exception {
    writeReadAndDelete(getBlockSize() * 2);
  }

  private void writeReadAndDelete(int len) throws IOException {
    Path path = getTestRootPath("test/hadoop/file");

    fc.mkdirs(path.getParent(), FsPermission.getDefault());

    FSDataOutputStream out = fc.create(path, EnumSet.of(CreateFlag.CREATE),
        CreateOpts.repFac((short) 1), CreateOpts.blockSize(getBlockSize()));
    out.write(data, 0, len);
    out.close();

    Assert.assertTrue("Exists", fc.exists(path));
    Assert.assertEquals("Length", len, fc.getFileStatus(path).getLen());

    FSDataInputStream in = fc.open(path);
    byte[] buf = new byte[len];
    in.readFully(0, buf);
    in.close();

    Assert.assertEquals(len, buf.length);
    for (int i = 0; i < buf.length; i++) {
      Assert.assertEquals("Position " + i, data[i], buf[i]);
    }

    Assert.assertTrue("Deleted", fc.delete(path, false));

    Assert.assertFalse("No longer exists", fc.exists(path));

  }

  @Test
  public void testOverwrite() throws IOException {
    Path path = getTestRootPath("test/hadoop/file");

    fc.mkdirs(path.getParent(), FsPermission.getDefault());

    createFile(path);

    Assert.assertTrue("Exists", fc.exists(path));
    Assert.assertEquals("Length", data.length, fc.getFileStatus(path).getLen());

    try {
      fc.create(path, EnumSet.of(CreateFlag.CREATE));
      Assert.fail("Should throw IOException.");
    } catch (IOException e) {
      // Expected
    }

    FSDataOutputStream out = fc.create(path,EnumSet.of(CreateFlag.OVERWRITE));
    out.write(data, 0, data.length);
    out.close();

    Assert.assertTrue("Exists", fc.exists(path));
    Assert.assertEquals("Length", data.length, fc.getFileStatus(path).getLen());

  }

  @Test
  public void testWriteInNonExistentDirectory() throws IOException {
    Path path = getTestRootPath("test/hadoop/file");
    Assert.assertFalse("Parent doesn't exist", fc.exists(path.getParent()));
    createFile(path);

    Assert.assertTrue("Exists", fc.exists(path));
    Assert.assertEquals("Length", data.length, fc.getFileStatus(path).getLen());
    Assert.assertTrue("Parent exists", fc.exists(path.getParent()));
  }

  @Test
  public void testDeleteNonExistentFile() throws IOException {
    Path path = getTestRootPath("test/hadoop/file");
    Assert.assertFalse("Doesn't exist", fc.exists(path));
    Assert.assertFalse("No deletion", fc.delete(path, true));
  }

  @Test
  public void testDeleteRecursively() throws IOException {
    Path dir = getTestRootPath("test/hadoop");
    Path file = getTestRootPath("test/hadoop/file");
    Path subdir = getTestRootPath("test/hadoop/subdir");

    createFile(file);
    Assert.assertTrue("Created subdir", fc.mkdirs(subdir, FsPermission.getDefault()));

    Assert.assertTrue("File exists", fc.exists(file));
    Assert.assertTrue("Dir exists", fc.exists(dir));
    Assert.assertTrue("Subdir exists", fc.exists(subdir));

    try {
      fc.delete(dir, false);
      Assert.fail("Should throw IOException.");
    } catch (IOException e) {
      // expected
    }
    Assert.assertTrue("File still exists", fc.exists(file));
    Assert.assertTrue("Dir still exists", fc.exists(dir));
    Assert.assertTrue("Subdir still exists", fc.exists(subdir));

    Assert.assertTrue("Deleted", fc.delete(dir, true));
    Assert.assertFalse("File doesn't exist", fc.exists(file));
    Assert.assertFalse("Dir doesn't exist", fc.exists(dir));
    Assert.assertFalse("Subdir doesn't exist", fc.exists(subdir));
  }

  @Test
  public void testDeleteEmptyDirectory() throws IOException {
    Path dir = getTestRootPath("test/hadoop");
    Assert.assertTrue(fc.mkdirs(dir, FsPermission.getDefault()));
    Assert.assertTrue("Dir exists", fc.exists(dir));
    Assert.assertTrue("Deleted", fc.delete(dir, false));
    Assert.assertFalse("Dir doesn't exist", fc.exists(dir));
  }

  @Test
  public void testRenameNonExistentPath() throws Exception {
    if (!renameSupported()) return;
    Path src = getTestRootPath("test/hadoop/NonExistingPath");
    Path dst = getTestRootPath("test/new/newpath");
    try {
      fc.rename(src, dst);
      Assert.assertTrue("rename of non existing path should have Assert.failed",
                                                    false);
    } catch (Exception e) {
      // expected
    }
  }

  @Test
  public void testRenameFileMoveToNonExistentDirectory() throws Exception {
    if (!renameSupported()) return;

    Path src = getTestRootPath("test/hadoop/file");
    createFile(src);
    Path dst = getTestRootPath("test/NonExisting/foo");
    rename(src, dst, false, true, false);
  }

  @Test
  public void testRenameFileMoveToExistingDirectory() throws Exception {
    if (!renameSupported()) return;

    Path src = getTestRootPath("test/hadoop/file");
    createFile(src);
    Path dst = getTestRootPath("test/Existing/newfile");
    fc.mkdirs(dst.getParent(), FsPermission.getDefault());
    rename(src, dst, true, false, true);
  }

  @Test
  public void testRenameFileAsExistingFile() throws Exception {
    if (!renameSupported()) return;

    Path src = getTestRootPath("test/hadoop/file");
    createFile(src);
    Path dst = getTestRootPath("test/existing/existingFile");
    createFile(dst);
    rename(src, dst, true, false, true);
  }

  @Test
  public void testRenameFileAsExistingDirectory() throws Exception {
    if (!renameSupported()) return;

    Path src = getTestRootPath("test/hadoop/file");
    createFile(src);
    Path dst = getTestRootPath("test/existing/existingDir");
    fc.mkdirs(dst, FsPermission.getDefault());
    rename(src, dst, true, false, true);
    Assert.assertTrue("Destination changed",
        fc.exists(getTestRootPath("test/existing/existingDir/file")));
  }

  @Test
  public void testRenameDirectoryMoveToNonExistentDirectory()
    throws Exception {
    if (!renameSupported()) return;

    Path src = getTestRootPath("test/hadoop/dir");
    fc.mkdirs(src, FsPermission.getDefault());
    Path dst = getTestRootPath("test/nonExisting/newdir");
    rename(src, dst, false, true, false);
  }

  @Test
  public void testRenameDirectoryMoveToExistingDirectory() throws Exception {
    if (!renameSupported()) return;

    Path src = getTestRootPath("test/hadoop/dir");
    fc.mkdirs(src, FsPermission.getDefault());
    createFile(getTestRootPath("test/hadoop/dir/file1"));
    createFile(getTestRootPath("test/hadoop/dir/subdir/file2"));

    Path dst = getTestRootPath("test/new/newdir");
    fc.mkdirs(dst.getParent(), FsPermission.getDefault());
    rename(src, dst, true, false, true);

    Assert.assertFalse("Nested file1 exists",
        fc.exists(getTestRootPath("test/hadoop/dir/file1")));
    Assert.assertFalse("Nested file2 exists",
        fc.exists(getTestRootPath("test/hadoop/dir/subdir/file2")));
    Assert.assertTrue("Renamed nested file1 exists",
        fc.exists(getTestRootPath("test/new/newdir/file1")));
    Assert.assertTrue("Renamed nested exists",
        fc.exists(getTestRootPath("test/new/newdir/subdir/file2")));
  }

  @Test
  public void testRenameDirectoryAsExistingFile() throws Exception {
    if (!renameSupported()) return;

    Path src = getTestRootPath("test/hadoop/dir");
    fc.mkdirs(src, FsPermission.getDefault());
    Path dst = getTestRootPath("test/new/newfile");
    createFile(dst);
    rename(src, dst, false, true, true);
  }

  @Test
  public void testRenameDirectoryAsExistingDirectory() throws Exception {
    if (!renameSupported()) return;

    Path src = getTestRootPath("test/hadoop/dir");
    fc.mkdirs(src, FsPermission.getDefault());
    createFile(getTestRootPath("test/hadoop/dir/file1"));
    createFile(getTestRootPath("test/hadoop/dir/subdir/file2"));

    Path dst = getTestRootPath("test/new/newdir");
    fc.mkdirs(dst, FsPermission.getDefault());
    rename(src, dst, true, false, true);
    Assert.assertTrue("Destination changed",
        fc.exists(getTestRootPath("test/new/newdir/dir")));
    Assert.assertFalse("Nested file1 exists",
        fc.exists(getTestRootPath("test/hadoop/dir/file1")));
    Assert.assertFalse("Nested file2 exists",
        fc.exists(getTestRootPath("test/hadoop/dir/subdir/file2")));
    Assert.assertTrue("Renamed nested file1 exists",
        fc.exists(getTestRootPath("test/new/newdir/dir/file1")));
    Assert.assertTrue("Renamed nested exists",
        fc.exists(getTestRootPath("test/new/newdir/dir/subdir/file2")));
  }

  @Test
  public void testInputStreamClosedTwice() throws IOException {
    //HADOOP-4760 according to Closeable#close() closing already-closed
    //streams should have no effect.
    Path src = getTestRootPath("test/hadoop/file");
    createFile(src);
    FSDataInputStream in = fc.open(src);
    in.close();
    in.close();
  }

  @Test
  public void testOutputStreamClosedTwice() throws IOException {
    //HADOOP-4760 according to Closeable#close() closing already-closed
    //streams should have no effect.
    Path src = getTestRootPath("test/hadoop/file");
    FSDataOutputStream out = fc.create(src, EnumSet.of(CreateFlag.CREATE));
    out.writeChar('H'); //write some data
    out.close();
    out.close();
  }

  protected void createFile(Path path) throws IOException {
    FSDataOutputStream out = fc.create(path, EnumSet.of(CreateFlag.CREATE));
    out.write(data, 0, data.length);
    out.close();
  }

  private void rename(Path src, Path dst, boolean renameShouldSucceed,
      boolean srcExists, boolean dstExists) throws IOException {
    try {
      fc.rename(src, dst);
      if (!renameShouldSucceed)
        Assert.fail("rename should have thrown exception");
    } catch (Exception e) {
      if (renameShouldSucceed)
        Assert.fail("rename should have suceeded, but threw exception");
    }

    Assert.assertEquals("Source exists", srcExists, fc.exists(src));
    Assert.assertEquals("Destination exists", dstExists, fc.exists(dst));
  }
}