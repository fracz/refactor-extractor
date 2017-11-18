/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.server.pm;

import android.annotation.Nullable;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.os.Environment;
import android.os.PowerManager;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.Log;
import android.util.Slog;

import com.android.internal.os.InstallerConnection.InstallerException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import dalvik.system.DexFile;

import static com.android.server.pm.Installer.DEXOPT_BOOTCOMPLETE;
import static com.android.server.pm.Installer.DEXOPT_DEBUGGABLE;
import static com.android.server.pm.Installer.DEXOPT_PROFILE_GUIDED;
import static com.android.server.pm.Installer.DEXOPT_PUBLIC;
import static com.android.server.pm.Installer.DEXOPT_SAFEMODE;
import static com.android.server.pm.InstructionSets.getAppDexInstructionSets;
import static com.android.server.pm.InstructionSets.getDexCodeInstructionSets;
import static com.android.server.pm.PackageManagerServiceCompilerMapping.getNonProfileGuidedCompilerFilter;

/**
 * Helper class for running dexopt command on packages.
 */
class PackageDexOptimizer {
    private static final String TAG = "PackageManager.DexOptimizer";
    static final String OAT_DIR_NAME = "oat";
    // TODO b/19550105 Remove error codes and use exceptions
    static final int DEX_OPT_SKIPPED = 0;
    static final int DEX_OPT_PERFORMED = 1;
    static final int DEX_OPT_FAILED = -1;

    private final Installer mInstaller;
    private final Object mInstallLock;

    private final PowerManager.WakeLock mDexoptWakeLock;
    private volatile boolean mSystemReady;

    PackageDexOptimizer(Installer installer, Object installLock, Context context,
            String wakeLockTag) {
        this.mInstaller = installer;
        this.mInstallLock = installLock;

        PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mDexoptWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag);
    }

    protected PackageDexOptimizer(PackageDexOptimizer from) {
        this.mInstaller = from.mInstaller;
        this.mInstallLock = from.mInstallLock;
        this.mDexoptWakeLock = from.mDexoptWakeLock;
        this.mSystemReady = from.mSystemReady;
    }

    static boolean canOptimizePackage(PackageParser.Package pkg) {
        return (pkg.applicationInfo.flags & ApplicationInfo.FLAG_HAS_CODE) != 0;
    }

    /**
     * Performs dexopt on all code paths and libraries of the specified package for specified
     * instruction sets.
     *
     * <p>Calls to {@link com.android.server.pm.Installer#dexopt} on {@link #mInstaller} are
     * synchronized on {@link #mInstallLock}.
     */
    int performDexOpt(PackageParser.Package pkg, String[] instructionSets, boolean checkProfiles,
            String targetCompilationFilter) {
        synchronized (mInstallLock) {
            final boolean useLock = mSystemReady;
            if (useLock) {
                mDexoptWakeLock.setWorkSource(new WorkSource(pkg.applicationInfo.uid));
                mDexoptWakeLock.acquire();
            }
            try {
                return performDexOptLI(pkg, instructionSets, checkProfiles,
                        targetCompilationFilter);
            } finally {
                if (useLock) {
                    mDexoptWakeLock.release();
                }
            }
        }
    }

    /**
     * Adjust the given dexopt-needed value. Can be overridden to influence the decision to
     * optimize or not (and in what way).
     */
    protected int adjustDexoptNeeded(int dexoptNeeded) {
        return dexoptNeeded;
    }

    /**
     * Adjust the given dexopt flags that will be passed to the installer.
     */
    protected int adjustDexoptFlags(int dexoptFlags) {
        return dexoptFlags;
    }

    private int performDexOptLI(PackageParser.Package pkg, String[] targetInstructionSets,
            boolean checkProfiles, String targetCompilerFilter) {
        final String[] instructionSets = targetInstructionSets != null ?
                targetInstructionSets : getAppDexInstructionSets(pkg.applicationInfo);

        if (!canOptimizePackage(pkg)) {
            return DEX_OPT_SKIPPED;
        }

        final List<String> paths = pkg.getAllCodePathsExcludingResourceOnly();
        final int sharedGid = UserHandle.getSharedAppGid(pkg.applicationInfo.uid);

        boolean isProfileGuidedFilter = DexFile.isProfileGuidedCompilerFilter(targetCompilerFilter);
        // If any part of the app is used by other apps, we cannot use profile-guided
        // compilation.
        // Skip the check for forward locked packages since they don't share their code.
        if (isProfileGuidedFilter && !pkg.isForwardLocked()) {
            for (String path : paths) {
                if (isUsedByOtherApps(path)) {
                    checkProfiles = false;

                    targetCompilerFilter = getNonProfileGuidedCompilerFilter(targetCompilerFilter);
                    if (DexFile.isProfileGuidedCompilerFilter(targetCompilerFilter)) {
                        throw new IllegalStateException(targetCompilerFilter);
                    }
                    isProfileGuidedFilter = false;

                    break;
                }
            }
        }

        // If we're asked to take profile updates into account, check now.
        boolean newProfile = false;
        if (checkProfiles && isProfileGuidedFilter) {
            // Merge profiles, see if we need to do anything.
            try {
                newProfile = mInstaller.mergeProfiles(sharedGid, pkg.packageName);
            } catch (InstallerException e) {
                Slog.w(TAG, "Failed to merge profiles", e);
            }
        }

        final boolean vmSafeMode = (pkg.applicationInfo.flags & ApplicationInfo.FLAG_VM_SAFE_MODE) != 0;
        final boolean debuggable = (pkg.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

        boolean performedDexOpt = false;
        boolean successfulDexOpt = true;

        final String[] dexCodeInstructionSets = getDexCodeInstructionSets(instructionSets);
        for (String dexCodeInstructionSet : dexCodeInstructionSets) {
            for (String path : paths) {
                int dexoptNeeded;
                try {
                    dexoptNeeded = DexFile.getDexOptNeeded(path,
                            dexCodeInstructionSet, targetCompilerFilter, newProfile);
                } catch (IOException ioe) {
                    Slog.w(TAG, "IOException reading apk: " + path, ioe);
                    return DEX_OPT_FAILED;
                }
                dexoptNeeded = adjustDexoptNeeded(dexoptNeeded);
                if (PackageManagerService.DEBUG_DEXOPT) {
                    Log.i(TAG, "DexoptNeeded for " + path + "@" + targetCompilerFilter + " is " +
                            dexoptNeeded);
                }

                final String dexoptType;
                String oatDir = null;
                switch (dexoptNeeded) {
                    case DexFile.NO_DEXOPT_NEEDED:
                        continue;
                    case DexFile.DEX2OAT_NEEDED:
                        dexoptType = "dex2oat";
                        oatDir = createOatDirIfSupported(pkg, dexCodeInstructionSet);
                        break;
                    case DexFile.PATCHOAT_NEEDED:
                        dexoptType = "patchoat";
                        break;
                    case DexFile.SELF_PATCHOAT_NEEDED:
                        dexoptType = "self patchoat";
                        break;
                    default:
                        throw new IllegalStateException("Invalid dexopt:" + dexoptNeeded);
                }

                Log.i(TAG, "Running dexopt (" + dexoptType + ") on: " + path + " pkg="
                        + pkg.applicationInfo.packageName + " isa=" + dexCodeInstructionSet
                        + " vmSafeMode=" + vmSafeMode + " debuggable=" + debuggable
                        + " target-filter=" + targetCompilerFilter + " oatDir = " + oatDir);
                // Profile guide compiled oat files should not be public.
                final boolean isPublic = !pkg.isForwardLocked() && !isProfileGuidedFilter;
                final int profileFlag = isProfileGuidedFilter ? DEXOPT_PROFILE_GUIDED : 0;
                final int dexFlags = adjustDexoptFlags(
                        ( isPublic ? DEXOPT_PUBLIC : 0)
                        | (vmSafeMode ? DEXOPT_SAFEMODE : 0)
                        | (debuggable ? DEXOPT_DEBUGGABLE : 0)
                        | profileFlag
                        | DEXOPT_BOOTCOMPLETE);

                try {
                    mInstaller.dexopt(path, sharedGid, pkg.packageName, dexCodeInstructionSet,
                            dexoptNeeded, oatDir, dexFlags, targetCompilerFilter, pkg.volumeUuid);
                    performedDexOpt = true;
                } catch (InstallerException e) {
                    Slog.w(TAG, "Failed to dexopt", e);
                    successfulDexOpt = false;
                }
            }
        }

        if (successfulDexOpt) {
            // If we've gotten here, we're sure that no error occurred. We've either
            // dex-opted one or more paths or instruction sets or we've skipped
            // all of them because they are up to date. In both cases this package
            // doesn't need dexopt any longer.
            return performedDexOpt ? DEX_OPT_PERFORMED : DEX_OPT_SKIPPED;
        } else {
            return DEX_OPT_FAILED;
        }
    }

    /**
     * Creates oat dir for the specified package. In certain cases oat directory
     * <strong>cannot</strong> be created:
     * <ul>
     *      <li>{@code pkg} is a system app, which is not updated.</li>
     *      <li>Package location is not a directory, i.e. monolithic install.</li>
     * </ul>
     *
     * @return Absolute path to the oat directory or null, if oat directory
     * cannot be created.
     */
    @Nullable
    private String createOatDirIfSupported(PackageParser.Package pkg, String dexInstructionSet) {
        if (!pkg.canHaveOatDir()) {
            return null;
        }
        File codePath = new File(pkg.codePath);
        if (codePath.isDirectory()) {
            File oatDir = getOatDir(codePath);
            try {
                mInstaller.createOatDir(oatDir.getAbsolutePath(), dexInstructionSet);
            } catch (InstallerException e) {
                Slog.w(TAG, "Failed to create oat dir", e);
                return null;
            }
            return oatDir.getAbsolutePath();
        }
        return null;
    }

    static File getOatDir(File codePath) {
        return new File(codePath, OAT_DIR_NAME);
    }

    void systemReady() {
        mSystemReady = true;
    }

    private boolean isUsedByOtherApps(String apkPath) {
        try {
            apkPath = new File(apkPath).getCanonicalPath();
        } catch (IOException e) {
            // Log an error but continue without it.
            Slog.w(TAG, "Failed to get canonical path", e);
        }
        String useMarker = apkPath.replace('/', '@');
        final int[] currentUserIds = UserManagerService.getInstance().getUserIds();
        for (int i = 0; i < currentUserIds.length; i++) {
            File profileDir = Environment.getDataProfilesDeForeignDexDirectory(currentUserIds[i]);
            File foreignUseMark = new File(profileDir, useMarker);
            if (foreignUseMark.exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * A specialized PackageDexOptimizer that overrides already-installed checks, forcing a
     * dexopt path.
     */
    public static class ForcedUpdatePackageDexOptimizer extends PackageDexOptimizer {

        public ForcedUpdatePackageDexOptimizer(Installer installer, Object installLock,
                Context context, String wakeLockTag) {
            super(installer, installLock, context, wakeLockTag);
        }

        public ForcedUpdatePackageDexOptimizer(PackageDexOptimizer from) {
            super(from);
        }

        @Override
        protected int adjustDexoptNeeded(int dexoptNeeded) {
            // Ensure compilation, no matter the current state.
            // TODO: The return value is wrong when patchoat is needed.
            return DexFile.DEX2OAT_NEEDED;
        }
    }
}