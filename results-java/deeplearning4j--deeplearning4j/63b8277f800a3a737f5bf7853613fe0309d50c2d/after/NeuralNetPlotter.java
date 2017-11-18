package org.deeplearning4j.plot;

import java.io.*;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.nn.NeuralNetwork;
import org.deeplearning4j.nn.gradient.NeuralNetworkGradient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;


/**
 * Credit to :
 * http://yosinski.com/media/papers/Yosinski2012VisuallyDebuggingRestrictedBoltzmannMachine.pdf
 *
 *
 * for visualizations
 * @author Adam Gibson
 *
 */
public class NeuralNetPlotter implements Serializable {

    private static 	ClassPathResource r = new ClassPathResource("/scripts/plot.py");
    private static Logger log = LoggerFactory.getLogger(NeuralNetPlotter.class);


    static {
        loadIntoTmp();
    }


    public void renderFilter(INDArray w,int r,int c,long length) {
        try {
            String filePath = writeMatrix(w);
            Process is = Runtime.getRuntime().exec("python /tmp/plot.py filter " + filePath + " " + r + " " + c + " " + length);
            log.info("Std out " + IOUtils.readLines(is.getInputStream()).toString());
            log.info("Rendering weights " + filePath);
            log.error(IOUtils.readLines(is.getErrorStream()).toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }




    public void plotNetworkGradient(NeuralNetwork network,NeuralNetworkGradient gradient,int patchesPerRow) {
        histogram(
                new String[]{"W", "hbias", "vbias", "w-gradient", "hbias-gradient", "vbias-gradient"},

                new INDArray[]{
                        network.getW(),
                        network.gethBias(),
                        network.getvBias(),
                        gradient.getwGradient(),
                        gradient.gethBiasGradient(),
                        gradient.getvBiasGradient()

                });
        plotActivations(network);

        FilterRenderer render = new FilterRenderer();
        try {
            if(network.getW().shape().length > 2) {
                INDArray w = (INDArray) network.getW().dup();
                INDArray render2 = w.transpose();
                render.renderFilters(render2, "currimg.png", w.columns() , w.rows(),w.slices());

            }
            else
                render.renderFilters(network.getW().dup(), "currimg.png", (int)Math.sqrt(network.getW().rows()) , (int) Math.sqrt( network.getW().rows()),patchesPerRow);


        } catch (Exception e) {
            log.error("Unable to plot filter, continuing...",e);
        }
    }



    /**
     * Histograms the given matrices. This is primarily used
     * for debugging gradients. You don't necessarily use this directly
     * @param titles the titles of the plots
     * @param matrices the matrices to plot
     */
    public void scatter(String[] titles, INDArray[] matrices) {
        String[] path = new String[matrices.length * 2];
        try {
            if(titles.length != matrices.length)
                throw new IllegalArgumentException("Titles and matrix lengths must be equal");


            for(int i = 0; i < path.length - 1; i+=2) {
                path[i] = writeMatrix(matrices[i / 2].ravel());
                path[i + 1] = titles[i / 2];
            }
            String paths = StringUtils.join(path,",");

            Process is = Runtime.getRuntime().exec("python /tmp/plot.py scatter " + paths);

            log.info("Rendering Matrix histograms... ");
            log.info("Std out " + IOUtils.readLines(is.getInputStream()).toString());
            log.error(IOUtils.readLines(is.getErrorStream()).toString());


        }catch(IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Histograms the given matrices. This is primarily used
     * for debugging gradients. You don't necessarily use this directly
     * @param titles the titles of the plots
     * @param matrices the matrices to plot
     */
    public void histogram(String[] titles, INDArray[] matrices) {
        String[] path = new String[matrices.length * 2];
        try {
            if(titles.length != matrices.length)
                throw new IllegalArgumentException("Titles and matrix lengths must be equal");


            for(int i = 0; i < path.length - 1; i+=2) {
                path[i] = writeMatrix(matrices[i / 2].ravel());
                path[i + 1] = titles[i / 2];
            }
            String paths = StringUtils.join(path,",");

            Process is = Runtime.getRuntime().exec("python /tmp/plot.py multi " + paths);

            log.info("Rendering Matrix histograms... ");
            log.info("Std out " + IOUtils.readLines(is.getInputStream()).toString());
            log.error(IOUtils.readLines(is.getErrorStream()).toString());


        }catch(IOException e) {
            throw new RuntimeException(e);
        }

    }



    protected String writeMatrix(INDArray matrix) throws IOException {
        String filePath = System.getProperty("java.io.tmpdir") + File.separator +  UUID.randomUUID().toString();
        File write = new File(filePath);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(write,true));
        write.deleteOnExit();
        for(int i = 0; i < matrix.rows(); i++) {
            INDArray row = matrix.getRow(i);
            StringBuffer sb = new StringBuffer();
            for(int j = 0; j < row.length(); j++) {
                sb.append(String.format("%.10f", row.getScalar(j)));
                if(j < row.length() - 1)
                    sb.append(",");
            }
            sb.append("\n");
            String line = sb.toString();
            bos.write(line.getBytes());
            bos.flush();
        }

        bos.close();
        return filePath;
    }



    public void plotActivations(NeuralNetwork network) {
        try {
            if(network.getInput() == null)
                throw new IllegalStateException("Unable to plot; missing input");

            INDArray hbiasMean = network.hBiasMean();


            String filePath = writeMatrix(hbiasMean);

            Process is = Runtime.getRuntime().exec("python /tmp/plot.py hbias " + filePath);

            Thread.sleep(10000);
            is.destroy();


            log.info("Rendering hbias " + filePath);
            log.error(IOUtils.readLines(is.getErrorStream()).toString());

        }catch(Exception e) {
            log.warn("Image closed");

        }
    }


    private static void loadIntoTmp() {

        File script = new File("/tmp/plot.py");


        try {
            List<String> lines = IOUtils.readLines(r.getInputStream());
            FileUtils.writeLines(script, lines);

        } catch (IOException e) {
            throw new IllegalStateException("Unable to load python file");

        }

    }

}