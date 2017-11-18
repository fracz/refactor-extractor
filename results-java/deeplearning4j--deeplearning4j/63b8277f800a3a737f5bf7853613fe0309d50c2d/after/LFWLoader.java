package org.deeplearning4j.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.deeplearning4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.linalg.dataset.DataSet;
import org.deeplearning4j.linalg.factory.NDArrays;
import org.deeplearning4j.linalg.util.ArrayUtil;
import org.deeplearning4j.linalg.util.FeatureUtil;
import org.deeplearning4j.util.ArchiveUtils;
import org.deeplearning4j.util.ImageLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads LFW faces data applyTransformToDestination. You can customize the size of the images as well
 * @author Adam Gibson
 *
 */
public class LFWLoader {

    private File baseDir = new File(System.getProperty("user.home"));
    public final static String LFW = "lfw";
    private File lfwDir = new File(baseDir,LFW);
    public final static String LFW_URL = "http://vis-www.cs.umass.edu/lfw/lfw.tgz";
    private File lfwTarFile = new File(lfwDir,"lfw.tgz");
    private static Logger log = LoggerFactory.getLogger(LFWLoader.class);
    private int numNames;
    private int numPixelColumns;
    private ImageLoader loader = new ImageLoader(28,28);
    private List<String> images = new ArrayList<String>();
    private List<String> outcomes = new ArrayList<String>();



    public LFWLoader() {
        this(28,28);
    }


    public LFWLoader(int imageWidth,int imageHeight) {
        loader = new ImageLoader(imageWidth,imageHeight);
    }

    public void getIfNotExists() throws Exception {
        if(!lfwDir.exists()) {
            lfwDir.mkdir();
            log.info("Grabbing LFW...");

            URL website = new URL(LFW_URL);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            if(!lfwTarFile.exists())
                 lfwTarFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(lfwTarFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.flush();
            IOUtils.closeQuietly(fos);
            rbc.close();
            log.info("Downloaded lfw");
            untarFile(baseDir,lfwTarFile);

        }


        File firstImage = null;
        try {
            firstImage = lfwDir.listFiles()[0].listFiles()[0];

        }catch(Exception e) {
            FileUtils.deleteDirectory(lfwDir);
            log.warn("Error opening first image; probably corrupt download...trying again",e);
            getIfNotExists();

        }


        //number of input neurons
        numPixelColumns = ArrayUtil.flatten(loader.fromFile(firstImage)).length;

        //each subdir is a person
        numNames = lfwDir.getAbsoluteFile().listFiles().length;

        @SuppressWarnings("unchecked")
        Collection<File> allImages = FileUtils.listFiles(lfwDir, org.apache.commons.io.filefilter.FileFileFilter.FILE, org.apache.commons.io.filefilter.DirectoryFileFilter.DIRECTORY);
        for(File f : allImages) {
            images.add(f.getAbsolutePath());
        }
        for(File dir : lfwDir.getAbsoluteFile().listFiles())
            outcomes.add(dir.getAbsolutePath());

    }



    public DataSet convertListPairs(List<DataSet> images) {
        INDArray inputs = NDArrays.create(images.size(), numPixelColumns);
        INDArray outputs = NDArrays.create(images.size(),numNames);

        for(int i = 0; i < images.size(); i++) {
            inputs.putRow(i,images.get(i).getFeatureMatrix());
            outputs.putRow(i,images.get(i).getLabels());
        }
        return new DataSet(inputs,outputs);
    }



    public DataSet getDataFor(int i) {
        File image = new File(images.get(i));
        int outcome = outcomes.indexOf(image.getParentFile().getAbsolutePath());
        try {
            return new DataSet(loader.asRowVector(image), FeatureUtil.toOutcomeVector(outcome, outcomes.size()));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to getFromOrigin data for image " + i + " for path " + images.get(i));
        }
    }

    /**
     * Get the first num found images
     * @param num the number of images to getFromOrigin
     * @return
     * @throws Exception
     */
    public List<DataSet> getFeatureMatrix(int num) throws Exception {
        List<DataSet> ret = new ArrayList<>(num);
        File[] files = lfwDir.listFiles();
        int label = 0;
        for(File file : files) {
            ret.addAll(getImages(label,file));
            label++;
            if(ret.size() >= num)
                break;
        }

        return ret;
    }

    public DataSet getAllImagesAsMatrix() throws Exception {
        List<DataSet> images = getImagesAsList();
        return convertListPairs(images);
    }


    public DataSet getAllImagesAsMatrix(int numRows) throws Exception {
        List<DataSet> images = getImagesAsList().subList(0, numRows);
        return convertListPairs(images);
    }

    public List<DataSet> getImagesAsList() throws Exception {
        List<DataSet> list = new ArrayList<>();
        File[] dirs = lfwDir.listFiles();
        for(int i = 0; i < dirs.length; i++) {
            list.addAll(getImages(i,dirs[i]));
        }
        return list;
    }

    public List<DataSet> getImages(int label,File file) throws Exception {
        File[] images = file.listFiles();
        List<DataSet> ret = new ArrayList<>();
        for(File f : images)
            ret.add(fromImageFile(label,f));
        return ret;
    }


    public DataSet fromImageFile(int label,File image) throws Exception {
        INDArray outcome = FeatureUtil.toOutcomeVector(label, numNames);
        INDArray image2 = ArrayUtil.toNDArray(loader.flattenedImageFromFile(image));
        return new DataSet(image2,outcome);
    }



    public  void untarFile(File baseDir, File tarFile) throws IOException {


        log.info("Untaring File: " + tarFile.toString());

        ArchiveUtils.unzipFileTo(tarFile.getAbsolutePath(),baseDir.getAbsolutePath());

    }




    public int getNumNames() {
        return numNames;
    }



    public int getNumPixelColumns() {
        return numPixelColumns;
    }



}