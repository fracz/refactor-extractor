package com.tencent.mm.androlib;

import com.tencent.mm.util.FileOperation;
import com.tencent.mm.util.TypedValue;
import com.tencent.mm.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class ResourceRepackage {

    private File mSignedApk;
    private File mSignedWith7ZipApk;
    private File mAlignedWith7ZipApk;
    private File m7zipOutPutDir;
    private File mStoredOutPutDir;

    private String mApkName;
    private File   mOutDir;
    private final String zipalignPath;
    private final String sevenZipPath;

    public ResourceRepackage(String zipalignPath, String zipPath ,File signedFile) {
        this.zipalignPath = zipalignPath;
        this.sevenZipPath = zipPath;
        mSignedApk = signedFile;
    }

    public void setOutDir(File outDir) {
        mOutDir = outDir;
    }

    public void repackageApk() throws IOException, InterruptedException {
        insureFileName();

        repackageWith7z();
        alignApk();
        deleteUnusedFiles();
    }

    private void deleteUnusedFiles() {
        //删除目录
        FileOperation.deleteDir(m7zipOutPutDir);
        FileOperation.deleteDir(mStoredOutPutDir);
        if (mSignedWith7ZipApk.exists()) {
            mSignedWith7ZipApk.delete();
        }

    }

    /**
     * 这边有点不太一样，就是当输出目录存在的时候是不会强制删除目录的
     *
     * @throws IOException
     */
    private void insureFileName() throws IOException {
        if (!mSignedApk.exists()) {
            throw new IOException(String.format(
                "can not found the signed apk file to repackage" +
                    ", path=%s",
                mSignedApk.getAbsolutePath()));
        }
        //需要自己安装7zip
        String apkBasename = mSignedApk.getName();
        mApkName = apkBasename.substring(0, apkBasename.indexOf(".apk"));
        //如果外面设过，就不用设了
        if (mOutDir == null) {
            mOutDir = new File(mSignedApk.getAbsoluteFile().getParent() + File.separator + mApkName);
        }

        mSignedWith7ZipApk = new File(mOutDir.getAbsolutePath() + File.separator + mApkName + "_channel_7zip.apk");
        mAlignedWith7ZipApk = new File(mOutDir.getAbsolutePath() + File.separator + mApkName + "_channel_7zip_aligned.apk");

        m7zipOutPutDir = new File(mOutDir.getAbsolutePath() + File.separator + TypedValue.OUT_7ZIP_FILE_PATH);
        mStoredOutPutDir = new File(mOutDir.getAbsolutePath() + File.separator + "storefiles");
        //删除目录,因为之前的方法是把整个输出目录都删除，所以不会有问题，现在不会，所以要单独�
        FileOperation.deleteDir(m7zipOutPutDir);
        FileOperation.deleteDir(mStoredOutPutDir);
        FileOperation.deleteDir(mSignedWith7ZipApk);
        FileOperation.deleteDir(mAlignedWith7ZipApk);
    }

    private void repackageWith7z() throws IOException, InterruptedException {
        System.out.printf("use 7zip to repackage: %s, will cost much more time\n", mSignedWith7ZipApk.getName());
        HashMap<String, Integer> compressData = FileOperation.unZipAPk(mSignedApk.getAbsolutePath(), m7zipOutPutDir.getAbsolutePath());
        //首先一次性生成一个全部都是压缩的安装包
        generalRaw7zip();

        ArrayList<String> storedFiles = new ArrayList<String>();
        //对于不压缩的要update回去
        for (String name : compressData.keySet()) {
            File file = new File(m7zipOutPutDir.getAbsolutePath() + File.separator + name);
            if (!file.exists()) {
                continue;
            }
            int method = compressData.get(name);
            if (method == TypedValue.ZIP_STORED) {
                storedFiles.add(name);
            }
        }

        addStoredFileIn7Zip(storedFiles);

        if (!mSignedWith7ZipApk.exists()) {
            throw new IOException(String.format(
                "7z repackage signed apk fail,you must install 7z command line version first, linux: p7zip, window: 7za, path=%s",
                mSignedWith7ZipApk.getAbsolutePath())
            );
        }
    }

    private void generalRaw7zip() throws IOException, InterruptedException {
        System.out.printf("general the raw 7zip file\n");
        Process pro;
        String outPath = m7zipOutPutDir.getAbsoluteFile().getAbsolutePath();

        String path = outPath + File.separator + "*";

        String cmd = Utils.isPresent(sevenZipPath) ? sevenZipPath : TypedValue.COMMAND_7ZIP;
        cmd += " a -tzip " + mSignedWith7ZipApk.getAbsolutePath() + " " + path + " -mx9";
        pro = Runtime.getRuntime().exec(cmd);

        InputStreamReader ir = new InputStreamReader(pro.getInputStream());
        LineNumberReader input = new LineNumberReader(ir);
        //如果不读会有问题，被阻塞
        while (input.readLine() != null) {
            ;
        }
        //destroy the stream
        if (pro != null) {
            pro.waitFor();
            pro.destroy();
        }
    }

    private void addStoredFileIn7Zip(ArrayList<String> storedFiles) throws IOException, InterruptedException {
        System.out.printf("rewrite the stored file into the 7zip, file count:%d\n", storedFiles.size());
        String storedParentName = mStoredOutPutDir.getAbsolutePath() + File.separator;
        String outputName = m7zipOutPutDir.getAbsolutePath() + File.separator;
        for (String name : storedFiles) {
            FileOperation.copyFileUsingStream(new File(outputName + name), new File(storedParentName + name));

        }
        storedParentName = storedParentName + File.separator + "*";

        //极限压缩
        String cmd = Utils.isPresent(sevenZipPath) ? sevenZipPath : TypedValue.COMMAND_7ZIP;
        cmd += " a -tzip " + mSignedWith7ZipApk.getAbsolutePath() + " " + storedParentName + " -mx0";
        Process pro = Runtime.getRuntime().exec(cmd);
        InputStreamReader ir = new InputStreamReader(pro.getInputStream());;
        LineNumberReader input = new LineNumberReader(ir);;
        //如果不读会有问题，被阻塞
        while (input.readLine() != null) {
            ;
        }
        //destroy the stream
        if (pro != null) {
            pro.waitFor();
            pro.destroy();
        }
    }

    private void alignApk() throws IOException, InterruptedException {
        if (mSignedWith7ZipApk.exists()) {
            alignApk(mSignedWith7ZipApk, mAlignedWith7ZipApk);
        }
    }

    private void alignApk(File before, File after) throws IOException, InterruptedException {
        System.out.printf("zipaligning apk: %s\n", before.getName());
        if (!before.exists()) {
            throw new IOException(
                String.format("can not found the raw apk file to zipalign, path=%s", before.getAbsolutePath())
            );
        }
        String cmd = Utils.isPresent(zipalignPath) ? zipalignPath : TypedValue.COMMAND_ZIPALIGIN;
        cmd += " 4 " + before.getAbsolutePath() + " " + after.getAbsolutePath();
        Process pro;
        pro = Runtime.getRuntime().exec(cmd);

        //destroy the stream
        pro.waitFor();
        pro.destroy();
    }
}