package de.julielab.ir.ltr.features;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import java.util.Arrays;
import java.util.List;

public class FeatureNormalizationUtils {
    public static double[] scaleFeatures(double[][] featureMatrix) {
        double[] maxFeatureValues = new double[featureMatrix[0].length];
        for (int i = 0; i < featureMatrix.length; i++) {
            double[] features = featureMatrix[i];
            // svm_node[] nodeVector = new svm_node[features.length];
            for (int j = 0; j < features.length; j++) {
                double featureValue = features[j];
                // svm_node node = new svm_node();
                // node.index = j;
                // node.value = featureValue;
                // nodeVector[j] = node;

                if (featureValue > maxFeatureValues[j])
                    maxFeatureValues[j] = featureValue;
            }
        }

        for (int i = 0; i < featureMatrix.length; i++) {
            double[] features = featureMatrix[i];
            for (int j = 0; j < features.length; j++) {
                features[j] /= maxFeatureValues[j];
            }
        }
        return maxFeatureValues;
    }

    public static double[] scaleFeatures(List<cc.mallet.types.FeatureVector> instances) {
        double[] maxFeatureValues = new double[instances.get(0).getAlphabet().size()];
        for (cc.mallet.types.FeatureVector fv : instances) {
            for (int i = 0; i < fv.numLocations(); i++) {
                int index = fv.getIndices()[i];
                double featureValue = fv.isBinary() ? 1 : fv.getValues()[i];

                if (featureValue > maxFeatureValues[index])
                    maxFeatureValues[index] = featureValue;
            }
        }

        for (cc.mallet.types.FeatureVector fv : instances) {
            for (int i = 0; i < fv.numLocations(); i++) {
                int index = fv.getIndices()[i];
                double value = fv.value(index);
                double maxFeatureValue = maxFeatureValues[index];
                double newValue = value / maxFeatureValue;
                fv.setValue(index, newValue);
            }
        }

        return maxFeatureValues;
    }

    public static void rangeScaleFeatures(double[] features, double[] maxFeatureValues) {
        for (int i = 0; i < features.length; i++) {
            features[i] /= maxFeatureValues[i];
        }
    }

    public static void rangeScaleFeatures(cc.mallet.types.FeatureVector fv, double[] maxFeatureValues) {
        for (int i = 0; i < fv.numLocations(); i++) {
            int index = fv.getIndices()[i];
            if (index < maxFeatureValues.length)
                fv.setValue(index, fv.getValues()[i] / maxFeatureValues[index]);
        }
    }

    public static double[] centerFeatures(double[][] featureMatrix) {
        double[] means = getFeatureMeans(featureMatrix);
        for (int i = 0; i < featureMatrix.length; i++) {
            double[] features = featureMatrix[i];
            for (int j = 0; j < features.length; j++) {
                features[j] -= means[j];
            }
        }
        return means;
    }

    public static double[] getFeatureMeans(double[][] featureMatrix) {
        double[] means = new double[featureMatrix[0].length];
        for (int i = 0; i < featureMatrix.length; i++) {
            double[] features = featureMatrix[i];
            for (int j = 0; j < features.length; j++) {
                double featureValue = features[j];
                means[j] += featureValue;
            }
        }
        for (int i = 0; i < means.length; i++) {
            means[i] /= featureMatrix.length;
        }
        return means;
    }

    public static double[] centerFeatures(InstanceList instances) {
        double[] means = getFeatureMeans(instances);
        for (Instance instance : instances) {
            cc.mallet.types.FeatureVector fv = (cc.mallet.types.FeatureVector) instance.getData();
            for (int j = 0; j < fv.numLocations(); j++) {
                int index = fv.getIndices()[j];
                fv.setValue(index, fv.getValues()[j] - means[index]);
            }
        }
        return means;
    }

    public static void centerFeatures(Instance instance, double[] featureMeans) {
        cc.mallet.types.FeatureVector fv = (cc.mallet.types.FeatureVector) instance.getData();
        for (int j = 0; j < fv.numLocations(); j++) {
            int index = fv.getIndices()[j];
            fv.setValue(index, fv.getValues()[j] - featureMeans[index]);
        }
    }

    public static double[] getFeatureMeans(InstanceList instances) {
        int numFeatures = instances.get(0).getDataAlphabet().size();
        double[] means = new double[numFeatures];
        for (Instance instance : instances) {
            cc.mallet.types.FeatureVector fv = (cc.mallet.types.FeatureVector) instance.getData();
            for (int j = 0; j < fv.numLocations(); j++) {
                int index = fv.getIndices()[j];
                double featureValue = fv.isBinary() ? 1 : fv.getValues()[j];
                means[index] += featureValue;
            }
        }
        for (int i = 0; i < means.length; i++) {
            means[i] /= numFeatures;
        }
        return means;
    }

    public static void centerFeatures(double[] features, double[] featureMeans) {
        for (int i = 0; i < features.length; i++) {
            features[i] -= featureMeans[i];
        }
    }


    public static StandardizationStats standardizeFeatures(double[][] featureMatrix) {
        double[] means = getFeatureMeans(featureMatrix);
        double[] stdDevs = getFeatureStdDeviations(featureMatrix, means);
        for (int i = 0; i < featureMatrix.length; i++) {
            double[] features = featureMatrix[i];
            for (int j = 0; j < features.length; j++) {
                features[j] -= means[j];
                features[j] /= stdDevs[j];
                if (Double.isNaN(features[j]))
                    features[j] = 0;
            }
        }
        return new StandardizationStats(means, stdDevs);
    }

    public static double[] getFeatureStdDeviations(double[][] featureMatrix, double[] means) {
        double[] stdDevs = new double[featureMatrix[0].length];
        for (int i = 0; i < featureMatrix.length; i++) {
            double[] features = featureMatrix[i];
            for (int j = 0; j < features.length; j++) {
                double featureValue = features[j];
                stdDevs[j] += Math.pow(featureValue - means[j], 2);
            }
        }

        for (int i = 0; i < stdDevs.length; i++) {
            stdDevs[i] /= featureMatrix.length;
            stdDevs[i] = Math.sqrt(stdDevs[i]);
        }
        return stdDevs;
    }

    public static StandardizationStats standardizeFeatures(InstanceList instances) {
        double[] means = getFeatureMeans(instances);
        double[] stdDevs = getFeatureStdDeviations(instances, means);

        for (Instance instance : instances) {
            assert instance.getData() instanceof cc.mallet.types.FeatureVector;
            cc.mallet.types.FeatureVector fv = (cc.mallet.types.FeatureVector) instance.getData();
            for (int j = 0; j < fv.numLocations(); j++) {
                int index = fv.getIndices()[j];
                double newVal = fv.getValues()[j] - means[index];
                newVal /= stdDevs[index];
                fv.setValue(index, newVal);
            }
        }
        return new StandardizationStats(means, stdDevs);
    }

    public static double[] getFeatureStdDeviations(InstanceList instances, double[] means) {
        int numFeatures = instances.get(0).getDataAlphabet().size();
        double[] stdDevs = new double[numFeatures];
        for (Instance instance : instances) {
            cc.mallet.types.FeatureVector fv = (cc.mallet.types.FeatureVector) instance.getData();
            for (int j = 0; j < fv.numLocations(); j++) {
                int index = fv.getIndices()[j];
                double featureValue = fv.isBinary() ? 1 : fv.getValues()[j];
                stdDevs[index] += Math.pow(featureValue - means[index], 2);
            }
        }
        for (int i = 0; i < means.length; i++) {
            stdDevs[i] /= numFeatures;
            stdDevs[i] = Math.sqrt(stdDevs[i]);
        }
        return stdDevs;
    }

    public static void standardizeFeatures(double[] features, double[] featureMeans, double[] featureStdDeviations) {
        if (features.length != featureMeans.length)
            throw new IllegalArgumentException(
                    "Feature dimension differs from the number of feature means. This means that the wrong model is being used for prediction. Feature dimension: "
                            + features.length + ", number of feature means: " + featureMeans.length);
        for (int i = 0; i < features.length; i++) {
            features[i] -= featureMeans[i];
            features[i] /= featureStdDeviations[i];
            if (Double.isNaN(features[i]))
                features[i] = 0;
        }
    }

    public static void standardizeFeatures(Instance instance, double[] featureMeans, double[] featureStdDeviations) {
        assert instance.getData() instanceof FeatureVector;
        cc.mallet.types.FeatureVector fv = (cc.mallet.types.FeatureVector) instance.getData();
        for (int i = 0; i < fv.numLocations(); i++) {
            int index = fv.getIndices()[i];
            double val = fv.getValues()[i] - featureMeans[index];
            val /= featureStdDeviations[index];
            fv.setValue(index, val);
        }
    }


    private static class FeatureVector {
        double[] features;
        private int index;

        public FeatureVector(double[] features, int index) {
            this.features = features;
            this.index = index;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(features);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FeatureVector other = (FeatureVector) obj;
            if (!Arrays.equals(features, other.features))
                return false;
            return true;
        }

    }
}
