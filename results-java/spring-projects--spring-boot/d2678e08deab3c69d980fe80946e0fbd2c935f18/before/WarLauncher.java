/*
 * Copyright 2012-2013 the original author or authors.
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

package org.springframework.boot.loader;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.loader.archive.Archive;

/**
 * {@link Launcher} for WAR based archives. This launcher for standard WAR archives.
 * Supports dependencies in {@code WEB-INF/lib} as well as {@code WEB-INF/lib-provided},
 * classes are loaded from {@code WEB-INF/classes}.
 *
 * @author Phillip Webb
 */
public class WarLauncher extends ExecutableArchiveLauncher {

	@Override
	public boolean isNestedArchive(Archive.Entry entry) {
		if (entry.isDirectory()) {
			return entry.getName().equals("WEB-INF/classes/");
		}
		else {
			return entry.getName().startsWith("WEB-INF/lib/")
					|| entry.getName().startsWith("WEB-INF/lib-provided/");
		}
	}

	@Override
	protected void postProcessClassPathArchives(List<Archive> archives) throws Exception {
		archives.add(0, getFilteredArchive());
	}

	/**
	 * Filter the specified WAR file to exclude elements that should not appear on the
	 * classpath.
	 * @return the filtered archive
	 * @throws IOException on error
	 */
	protected Archive getFilteredArchive() throws IOException {
		return getArchive().getFilteredArchive(new Archive.EntryRenameFilter() {
			@Override
			public String apply(String entryName, Archive.Entry entry) {
				if (entryName.startsWith("META-INF/") || entryName.startsWith("WEB-INF/")) {
					return null;
				}
				return entryName;
			}
		});
	}

	public static void main(String[] args) {
		new WarLauncher().launch(args);
	}
}