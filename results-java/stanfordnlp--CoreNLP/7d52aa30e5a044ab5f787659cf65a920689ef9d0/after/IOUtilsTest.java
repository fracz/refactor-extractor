package edu.stanford.nlp.io;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import edu.stanford.nlp.util.StringUtils;

import junit.framework.Assert;
import junit.framework.TestCase;

public class IOUtilsTest extends TestCase {

  String dirPath;
  File dir;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dir = File.createTempFile("IOUtilsTest", ".dir");
    assertTrue(dir.delete());
    assertTrue(dir.mkdir());
    dirPath = dir.getAbsolutePath();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    this.delete(this.dir);
  }

  public void testReadWriteStreamFromString() throws IOException, ClassNotFoundException {
    ObjectOutputStream oos = IOUtils.writeStreamFromString(dirPath + "/objs.obj");
    oos.writeObject(Integer.valueOf(42));
    oos.writeObject("forty two");
    oos.close();
    ObjectInputStream ois = IOUtils.readStreamFromString(dirPath + "/objs.obj");
    Object i = ois.readObject();
    Object s = ois.readObject();
    Assert.assertTrue(Integer.valueOf(42).equals(i));
    Assert.assertTrue("forty two".equals(s));
    ois.close();
  }

  public void testReadLines() throws Exception {
    File file = new File(this.dir, "lines.txt");
    Iterable<String> iterable;

    write("abc", file);
    iterable = IOUtils.readLines(file);
    Assert.assertEquals("abc", StringUtils.join(iterable, "!"));
    Assert.assertEquals("abc", StringUtils.join(iterable, "!"));

    write("abc\ndef\n", file);
    iterable = IOUtils.readLines(file);
    Assert.assertEquals("abc!def", StringUtils.join(iterable, "!"));
    Assert.assertEquals("abc!def", StringUtils.join(iterable, "!"));

    write("\na\nb\n", file);
    iterable = IOUtils.readLines(file.getPath());
    Assert.assertEquals("!a!b", StringUtils.join(iterable, "!"));
    Assert.assertEquals("!a!b", StringUtils.join(iterable, "!"));

    write("", file);
    iterable = IOUtils.readLines(file);
    Assert.assertFalse(iterable.iterator().hasNext());

    write("\n", file);
    iterable = IOUtils.readLines(file.getPath());
    Iterator<String> iterator = iterable.iterator();
    Assert.assertTrue(iterator.hasNext());
    iterator.next();

    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
        new GZIPOutputStream(new FileOutputStream(file))));
    writer.write("\nzipped\ntext\n");
    writer.close();
    iterable = IOUtils.readLines(file, GZIPInputStream.class);
    Assert.assertEquals("!zipped!text", StringUtils.join(iterable, "!"));
    Assert.assertEquals("!zipped!text", StringUtils.join(iterable, "!"));
  }

  public void testIterFilesRecursive() throws IOException {
    File dir = new File(this.dir, "recursive");
    File a = new File(dir, "x/a");
    File b = new File(dir, "x/y/b.txt");
    File c = new File(dir, "c.txt");
    File d = new File(dir, "dtxt");

    write("A", a);
    write("B", b);
    write("C", c);
    write("D", d);

    Set<File> actual = toSet(IOUtils.iterFilesRecursive(dir));
    Assert.assertEquals(toSet(Arrays.asList(a, b, c, d)), actual);

    actual = toSet(IOUtils.iterFilesRecursive(dir, ".txt"));
    Assert.assertEquals(toSet(Arrays.asList(b, c)), actual);

    actual = toSet(IOUtils.iterFilesRecursive(dir, Pattern.compile(".txt")));
    Assert.assertEquals(toSet(Arrays.asList(b, c, d)), actual);
  }

  protected void delete(File file) {
    if (file.isDirectory()) {
      for (File child: file.listFiles()) {
        this.delete(child);
      }
    }
    // Use an Assert here to make sure that all files were closed properly
    Assert.assertTrue(file.delete());
  }

  protected static void write(String text, File file) throws IOException {
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    FileWriter writer = new FileWriter(file);
    writer.write(text);
    writer.close();
  }

  public static <E> Set<E> toSet(Iterable<E> iter) {
    Set<E> set = new HashSet<E>();
    for (E item: iter) {
      set.add(item);
    }
    return set;
  }

  /**
   * Tests that slurpFile can get files from within the classpath
   */
  public void testSlurpFile() {
    String contents;
    try {
      contents = IOUtils.slurpFile("edu/stanford/nlp/io/test.txt", "utf-8");
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }

    assertEquals("This is a test sentence.", contents.trim());

    try {
      contents = IOUtils.slurpFile("edu/stanford/nlp/io/test.txt");
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }

    assertEquals("This is a test sentence.", contents.trim());

    try {
      contents = IOUtils.slurpFile("edu/stanford/nlp/io/test.txtzzz");
      throw new AssertionError("Should not have found unknown file");
    } catch (IOException e) {
      // yay
    }

    contents = IOUtils.slurpFileNoExceptions("edu/stanford/nlp/io/test.txt");
    assertEquals("This is a test sentence.", contents.trim());


    try {
      contents = IOUtils.slurpFileNoExceptions("edu/stanford/nlp/io/test.txtzzz");
      throw new AssertionError("Should not have found unknown file");
    } catch (RuntimeIOException e) {
      // yay
    }
  }
}