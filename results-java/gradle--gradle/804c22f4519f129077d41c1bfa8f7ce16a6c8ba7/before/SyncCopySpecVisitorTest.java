/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.file.copy;

import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RelativePath;
import org.gradle.api.internal.file.collections.DirectoryFileTree;
import org.gradle.test.fixtures.file.TestFile;
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;

@RunWith(JMock.class)
public class SyncCopySpecVisitorTest {
    @Rule
    public final TestNameTestDirectoryProvider tmpDir = new TestNameTestDirectoryProvider();
    private final JUnit4Mockery context = new JUnit4Mockery();
    private final CopySpecContentVisitor delegate = context.mock(CopySpecContentVisitor.class);
    private SyncCopySpecContentVisitor visitor;

    @Before
    public void setUp() {
        context.checking(new Expectations(){{
            allowing(delegate).startVisit();
            allowing(delegate).visit(with(notNullValue(FileCopyDetailsInternal.class)));
            allowing(delegate).visit(with(notNullValue(FileCopyDetailsInternal.class)));
            allowing(delegate).endVisit();
        }});
    }

    @Test
    public void deletesExtraFilesFromDestinationDirectoryAtTheEndOfVisit() {
        TestFile destDir = tmpDir.createDir("dest");
        destDir.createFile("subdir/included.txt");
        destDir.createFile("subdir/extra.txt");
        destDir.createFile("included.txt");
        destDir.createFile("extra.txt");

        visitor(destDir);
        visitor.startVisit();
        visitor.visit(dir("subdir"));
        visitor.visit(file("subdir/included.txt"));
        visitor.visit(file("included.txt"));
        visitor.endVisit();

        destDir.assertHasDescendants("subdir/included.txt", "included.txt");
    }

    @Test
    public void deletesExtraDirectoriesFromDestinationDirectoryAtTheEndOfVisit() throws Exception {
        TestFile destDir = tmpDir.createDir("dest");
        destDir.createFile("included.txt");
        destDir.createFile("extra/extra.txt");

        visitor(destDir);
        visitor.startVisit();
        visitor.visit(file("included.txt"));

        // TODO - delete these
        Field field = SyncCopySpecContentVisitor.class.getDeclaredField("visited");
        field.setAccessible(true);
        Set visited = (Set) field.get(visitor);
        assert visited.contains(new RelativePath(true, "included.txt"));
        assert !visited.contains(new RelativePath(true, "extra", "extra.txt"));
        final Set<RelativePath> actual = new HashSet<RelativePath>();
        new DirectoryFileTree(destDir).postfix().visit(new FileVisitor() {
            public void visitDir(FileVisitDetails dirDetails) {
            }

            public void visitFile(FileVisitDetails fileDetails) {
                actual.add(fileDetails.getRelativePath());
            }
        });
        assert actual.contains(new RelativePath(true, "included.txt"));
        assert actual.contains(new RelativePath(true, "extra", "extra.txt"));

        visitor.endVisit();

        destDir.assertHasDescendants("included.txt");
    }

    @Test
    public void doesNotDeleteDestDirectoryWhenNothingCopied() {
        TestFile destDir = tmpDir.createDir("dest");
        destDir.createFile("extra.txt");
        destDir.createFile("extra/extra.txt");

        visitor(destDir);
        visitor.startVisit();
        visitor.endVisit();

        destDir.assertHasDescendants();
    }

    @Test
    public void didWorkWhenDelegateDidWork() {
        visitor(tmpDir.createDir("test"));
        context.checking(new Expectations() {{
            allowing(delegate).getDidWork();
            will(returnValue(true));
        }});

        assertTrue(visitor.getDidWork());
    }

    @Test
    public void didWorkWhenFilesDeleted() {
        TestFile destDir = tmpDir.createDir("dest");
        destDir.createFile("extra.txt");

        visitor(destDir);
        visitor.startVisit();
        visitor.endVisit();

        assertTrue(visitor.getDidWork());
    }

    private SyncCopySpecContentVisitor visitor(final File destDir) {
        visitor = new SyncCopySpecContentVisitor(delegate, destDir);
        return visitor;
    }

    private FileCopyDetailsInternal file(final String path) {
        return file(RelativePath.parse(true, path));
    }

    private FileCopyDetailsInternal dir(final String path) {
        final RelativePath relativePath = RelativePath.parse(false, path);
        final FileCopyDetailsInternal details = context.mock(FileCopyDetailsInternal.class, relativePath.toString());

        context.checking(new Expectations(){{
            allowing(details).getRelativePath();
            will(returnValue(relativePath));
        }});

        return details;
    }

    private FileCopyDetailsInternal file(final RelativePath relativePath) {
        final FileCopyDetailsInternal details = context.mock(FileCopyDetailsInternal.class, relativePath.toString());

        context.checking(new Expectations(){{
            allowing(details).getRelativePath();
            will(returnValue(relativePath));
        }});

        return details;
    }
}