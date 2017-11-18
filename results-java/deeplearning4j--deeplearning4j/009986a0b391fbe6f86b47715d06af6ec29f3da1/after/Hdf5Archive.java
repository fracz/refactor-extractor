package org.deeplearning4j.nn.modelimport.keras;

import com.sun.tools.doclint.HtmlTag;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.hdf5;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.shade.jackson.databind.DeserializationFeature;
import org.nd4j.shade.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.hdf5.H5F_ACC_RDONLY;
import static org.bytedeco.javacpp.hdf5.H5O_TYPE_DATASET;
import static org.bytedeco.javacpp.hdf5.H5O_TYPE_GROUP;

/**
 * Created by davekale on 1/17/17.
 */
public class Hdf5Archive {
    private hdf5.H5File file;

    public Hdf5Archive(String archiveFilename) {
        this.file = new hdf5.H5File(archiveFilename, H5F_ACC_RDONLY);
    }

    public INDArray readDataSet(String datasetName, String... groups) throws UnsupportedKerasConfigurationException {
        hdf5.CommonFG group = this.file.asCommonFG();
        for (int i = 0; i < groups.length; i++)
            group = group.openGroup(groups[i]).asCommonFG();
        return readDataSet(group, datasetName);
    }

    public String readAttributeAsJson(String attributeName, String... groups) throws UnsupportedKerasConfigurationException {
        if (groups.length == 0)
            return readAttributeAsJson(this.file.openAttribute(attributeName));
        hdf5.Group group = this.file.asCommonFG().openGroup(groups[0]);
        for (int i = 1; i < groups.length; i++)
            group = group.asCommonFG().openGroup(groups[i]);
        return readAttributeAsJson(group.openAttribute(attributeName));
    }

    public List<String> getDataSets(String... groups) {
        hdf5.CommonFG group = this.file.asCommonFG();
        for (int i = 0; i < groups.length; i++)
            group = group.openGroup(groups[i]).asCommonFG();
        return getObjects(group, H5O_TYPE_DATASET);
    }

    public List<String> getGroups(String... groups) {
        hdf5.CommonFG group = this.file.asCommonFG();
        for (int i = 0; i < groups.length; i++)
            group = group.openGroup(groups[i]).asCommonFG();
        return getObjects(group, H5O_TYPE_GROUP);
    }

    private INDArray readDataSet(hdf5.CommonFG fileGroup, String datasetName) throws UnsupportedKerasConfigurationException {
        hdf5.DataSet dataset = fileGroup.openDataSet(datasetName);
        hdf5.DataSpace space = dataset.getSpace();
        int nbDims = space.getSimpleExtentNdims();
        long[] dims = new long[nbDims];
        space.getSimpleExtentDims(dims);
        float[] dataBuffer = null;
        FloatPointer fp = null;
        int j = 0;
        INDArray data = null;
        switch (nbDims) {
            case 4: /* 2D Convolution weights */
                dataBuffer = new float[(int)(dims[0]*dims[1]*dims[2]*dims[3])];
                fp = new FloatPointer(dataBuffer);
                dataset.read(fp, new hdf5.DataType(hdf5.PredType.NATIVE_FLOAT()));
                fp.get(dataBuffer);
                data = Nd4j.create((int)dims[0], (int)dims[1], (int)dims[2], (int)dims[3]);
                j = 0;
                for (int i1 = 0; i1 < dims[0]; i1++)
                    for (int i2 = 0; i2 < dims[1]; i2++)
                        for (int i3 = 0; i3 < dims[2]; i3++)
                            for (int i4 = 0; i4 < dims[3]; i4++)
                                data.putScalar(i1, i2, i3, i4, dataBuffer[j++]);
                break;
            case 2: /* Dense and Recurrent weights */
                dataBuffer = new float[(int)(dims[0]*dims[1])];
                fp = new FloatPointer(dataBuffer);
                dataset.read(fp, new hdf5.DataType(hdf5.PredType.NATIVE_FLOAT()));
                fp.get(dataBuffer);
                data = Nd4j.create((int)dims[0], (int)dims[1]);
                j = 0;
                for (int i1 = 0; i1 < dims[0]; i1++)
                    for (int i2 = 0; i2 < dims[1]; i2++)
                        data.putScalar(i1, i2, dataBuffer[j++]);
                break;
            case 1: /* Bias */
                dataBuffer = new float[(int)dims[0]];
                fp = new FloatPointer(dataBuffer);
                dataset.read(fp, new hdf5.DataType(hdf5.PredType.NATIVE_FLOAT()));
                fp.get(dataBuffer);
                data = Nd4j.create((int)dims[0]);
                j = 0;
                for (int i1 = 0; i1 < dims[0]; i1++)
                    data.putScalar(i1, dataBuffer[j++]);
                break;
            default:
                throw new UnsupportedKerasConfigurationException("Cannot import weights with rank " + nbDims);
        }
        return data;
    }

    private List<String> getObjects(hdf5.CommonFG fileGroup, int objType) {
        List<String> groups = new ArrayList<String>();
        for (int i = 0; i < fileGroup.getNumObjs(); i++) {
            BytePointer objPtr = fileGroup.getObjnameByIdx(i);
            if (fileGroup.childObjType(objPtr) == objType)
                groups.add(fileGroup.getObjnameByIdx(i).getString());
        }
        return groups;
    }

    private String readAttributeAsJson(hdf5.Attribute attribute) throws UnsupportedKerasConfigurationException {
        hdf5.VarLenType vl = attribute.getVarLenType();
        int bufferSizeMult = 1;
        String s = null;
        /* TODO: find a less hacky way to do this.
         * Reading variable length strings (from attributes) is a giant
         * pain. There does not appear to be any way to determine the
         * length of the string in advance, so we use a hack: choose a
         * buffer size and read the config. If Jackson fails to parse
         * it, then we must not have read the entire config. Increase
         * buffer and repeat.
         */
        while (true) {
            byte[] attrBuffer = new byte[bufferSizeMult * 2000];
            BytePointer attrPointer = new BytePointer(attrBuffer);
            attribute.read(vl, attrPointer);
            attrPointer.get(attrBuffer);
            s = new String(attrBuffer);
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
            try {
                mapper.readTree(s);
                break;
            } catch (IOException e) {}
            bufferSizeMult++;
            if (bufferSizeMult > 100) {
                throw new UnsupportedKerasConfigurationException("Could not read abnormally long HDF5 attribute");
            }
        }
        return s;
    }
}