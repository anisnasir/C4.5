/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package c45;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author Adi
 */
public class C45 {

    Instances fullData;
    Instances data;
    int MIN_OBJECTS = 5;

    C45() {
        super();
        //rules = "";
    }

    public void setData(Instances d) {
        fullData = d;
    }


    public String buildTree() {
        Vector rules = new Vector();
        for (int i = 0; i < fullData.num_instances; i++) {
            Rule newRule=getRules(fullData, fullData.instance(i));
            if(!rules.contains(newRule))
                rules.add(newRule);
        }
        ListIterator it = rules.listIterator();
        StringBuffer completeRule = new StringBuffer();
        while (it.hasNext()) {
            completeRule.append(it.next());
            completeRule.append("\n");
        }
        return completeRule.toString();
    }

    /**
     * Get rules by inputting a new instance
     * @param d data set
     * @param test new instance
     * @return rule generated
     */
    Rule getRules(Instances d, String[] newInstance) {
        data = d;
        boolean isInitial = true;
        //rules = "";
        Rule ruleSet = new Rule();
        Instances currInst = new Instances();
        currInst.attributes = (Vector) data.attributes.clone();
        currInst.num_instances = data.num_instances;
        currInst.num_attributes = data.num_attributes;
        currInst.class_index = data.class_index;
        int n = 0;
        while (loopBreak() == false && n < 5) {
            n++;
            int i = selectFeature();
            
            String attributeName = data.getAttributeName(i);
            String value = newInstance[i];
            Vector v = new Vector();
            if (data.getAttributeType(i).trim().compareTo("DISCRETE") == 0) {
                ruleSet.addCondition(new Condition(attributeName, i, Condition.EQ, value));
                //add instances satisfying rule
                for (int j = 0; j < data.num_instances; j++) {
                    if (data.data_array[j][i].trim().compareTo(value.trim()) == 0) {
                        //v.add(data.instance(j));
                        v.add(data.data_array[j]);
                    }
                }
                if (isInitial) {
                    data = currInst;
                    isInitial = false;
                }
                //use data array instead
                data.data = v;
                data.num_instances = v.size();
                data.convert();
                //rules = rules + " && ";
            } else {
                double threshold_value = getThreshold(i);
                //double test_value = Double.parseDouble(data.value(test, i));
                double test_value = Double.parseDouble(newInstance[i]);
                if (test_value >= threshold_value) {
                    ruleSet.addCondition(new Condition(attributeName, i, Condition.GTE, threshold_value+""));
                    for (int j = 0; j < data.num_instances; j++) {
                        Double num = Double.parseDouble(data.data_array[j][i]);
                        if (num >= threshold_value) {
                            //v.add(data.instance(j));
                            v.add(data.data_array[j]);
                        }
                    }
                } else {
                    ruleSet.addCondition(new Condition(attributeName, i, Condition.LT, threshold_value+""));
                    for (int j = 0; j < data.num_instances; j++) {
                        Double num = Double.parseDouble(data.data_array[j][i]);
                        if (num < threshold_value) {
                            v.add(data.data_array[j]);
                        }
                    }
                }
                if (isInitial) {
                    data = currInst;
                    isInitial = false;
                }
                data.data = v;
                data.num_instances = v.size();
                data.convert();
            }
        }
        ruleSet.label=getLeaf();
        return ruleSet;
    }

    /**
     * 
     * @return 
     */
    boolean loopBreak() {
        if (data.num_instances < MIN_OBJECTS) {
            return true;
        }
        boolean flag = true;
        String[] temp = data.getAttributeValues(data.class_index);
        if (temp.length == 0) {
            return false;
        }
        String test = temp[0];
        for (int j = 0; j < temp.length; j++) {
            for (int i = 0; i < temp.length; i++) {
                if (test.compareTo(temp[i]) != 0) {
                    flag = false;
                }
            }
        }
        return flag;
    }

    String getLeaf() {
        return data.data_array[0][data.getClassIndex()];
    }

    /**
     * Calculate gain
     * @return 
     */
    double calculateGain() {
        //values in the class
        String[] attribValues = data.getAttributeValues(data.getClassIndex());
        //possible class values
        String[] array1 = discreteOutputValues(data.getClassIndex());
        int[] nk = new int[array1.length];
        //count how many elements in the class have a particular value
        for (int i = 0; i < attribValues.length; i++) {
            for (int j = 0; j < array1.length; j++) {
                if (attribValues[i].compareTo(array1[j]) == 0) {
                    nk[j]++;
                }
            }
        }
        //calculate entropy
        double entropy = 0;
        int total = attribValues.length;
        for (int i = 0; i < nk.length; i++) {
            double temp = nk[i] / (float) total;
            if (temp == 0 || total == 0) {
                continue;
            }
            entropy = entropy + (-1 * temp) * (Math.log(temp) / Math.log(2.0));
        }

        return entropy;
    }

    /**
     * Select attribute with highest gain
     * @return index of attribute
     */
    int selectFeature() {
        double[] entropyArr = new double[data.num_attributes - 1];
        for (int i = 0; i < data.num_attributes - 1; i++) {
            String[] temp = data.getAllAttributeTypes();
            //System.out.println(temp[i]);
            if (temp[i].compareTo("REAL") == 0) {
                entropyArr[i] = calculateContinuousEntropy(i);
            } else {                
                entropyArr[i] = (calculateGain() - calculateDiscreteEntropy(i)) / splitInfoDiscrete(i);
            }
        }
        int max = 0;
        for (int i = 0; i < entropyArr.length; i++) {
            if (entropyArr[max] < entropyArr[i]) {
                max = i;
            }
        }
        return max;
    }

    /**
     * Retrieve information of discrete values
     * @param i attribute column (class)
     * @return information
     */
    double splitInfoDiscrete(int i) {
        String[] attributeValues = data.getAttributeValues(i);
        String[] array2 = data.getAttributeRange(i);
        int p_values = array2.length;
        //store number of instances having belonging to a certain class
        int[] nk = new int[p_values];
        for (int j = 0; j < array2.length; j++) {
            for (int k = 0; k < attributeValues.length; k++) {
                if (attributeValues[k].compareTo(array2[j]) == 0) {
                    nk[j]++;
                }
            }
        }
        float info = 0;
        for (int x = 0; x < nk.length; x++) {            
            float tempo = nk[x] / (float) data.num_instances;
            if (tempo != 0 && tempo != 1) {
                info += (-1 * tempo) * (Math.log(tempo) / Math.log(2.0));
            }
            //System.out.println(info);
        }
        if (info == 0) {
            return .000001;
        }
        return info;
    }

    /**
     * Calculate entropy of discrete class
     * @param i attribute column
     * @return 
     */
    double calculateDiscreteEntropy(int i) {
        String[] outputValues = discreteOutputValues(i);
        double entropy = 0.0;
        for (int j = 0; j < outputValues.length; j++) {
            //System.out.println("--"+outputValues[j]);
            double temp = getPartialDataSize(outputValues[j], i) / (float) data.num_instances;
            entropy = entropy + (temp) * getEntropy(getPartialData(outputValues[j], i));
        }
        return entropy;
    }

    /**
     * Entropy based on split
     * @param i entropy's split info
     * @return 
     */
    double splitInfo(int i) {
        int total = data.num_instances;
        double entropy = 0;
        entropy = entropy + (-1 * i / (float) total) * (Math.log(i / (float) total) / Math.log(2.0));
        entropy = entropy + (-1 * (total - i) / (float) total) * (Math.log((total - i) / (float) total) / Math.log(2.0));
        return entropy;
    }

    /**
     * Get Gain for a real valued attribute
     * @param i attribute column
     * @return 
     */
    double calculateContinuousEntropy(int i) {
        //get attrib values
        String[] in_attrib = data.getAttributeValues(i);
        //get class values
        String[] out_attrib = data.getAttributeValues(data.getClassIndex());

        //sort array according to attribute column
        for (int j = 0; j < in_attrib.length; j++) {
            for (int k = 0; k < in_attrib.length - 1; k++) {
                if (Double.parseDouble(in_attrib[k]) > Double.parseDouble(in_attrib[k + 1])) {
                    String temp = in_attrib[k];
                    String temp1 = out_attrib[k];
                    in_attrib[k] = in_attrib[k + 1];
                    out_attrib[k] = out_attrib[k + 1];
                    in_attrib[k + 1] = temp;
                    out_attrib[k + 1] = temp1;
                }
            }
        }

        String[] out_values = data.getAttributeRange(data.getClassIndex());
        //formula based on distinct attribute values
        int N = countDistinct(in_attrib);
        double M1 = 0;
        if (N < 2) {
            return 0;
        } else {
            M1 = Math.log((double) N - 1) / Math.log((double) 2);
            M1 = M1 / data.num_instances;
        }

        int size = data.num_instances;
        //Calculate gain 
        double out_info = calculateGain();
        double[] threshold = new double[in_attrib.length];

        //number of possible class values
        int out_size = out_values.length;
        int count = 1;
        double minsplit = 0.10 * size / (out_size);
        int lowitems = 0;
        //int highitems = size - lowitems;
        if (minsplit <= 2) {
            minsplit = 2;
        } else if (minsplit > 25) {
            minsplit = 25;
        }

        double t1, t2;
        int tries = 0;
        double threshcost = 0;


        for (int k = 1; k < size - 1; k++) {
            lowitems += 1;
            if (lowitems < minsplit) {
                continue;
            } else if (lowitems > (size - minsplit)) {
                break;
            }

            t1 = Double.parseDouble(in_attrib[k]);
            t2 = Double.parseDouble(in_attrib[k + 1]);
            if (t1 < t2 - 1E-5) {
                tries++;
            } else {
                count++;
                continue;
            }
        }
        if (tries > 0) {
            threshcost = (Math.log((double) tries) / (double) size) / Math.log((double) 2);
        } else {
            threshcost = 0;
        }


        double max = 0;
        for (int k = 1; k < size - 1; k++) {
            if (in_attrib[k].compareTo(in_attrib[k - 1]) != 0) {                
                threshold[k] = calculateThreshold(out_attrib, out_values, k);
                threshold[k] = (out_info - threshold[k] - M1) / splitInfo(k);
                threshold[k] = threshold[k] - threshcost;
                if (threshold[k] > max) {
                    max = threshold[k];
                }
            } else {
                continue;
            }
        }
        //System.out.println(max);
        return max;



    }

    /**
     * Count number of distinct values in array
     * @param in array containing values
     * @return 
     */
    int countDistinct(String[] in) {
        int n = 1;
        for (int j = 0; j < in.length; j++) {
            for (int k = 0; k < in.length - 1; k++) {
                if (Double.parseDouble(in[k]) > Double.parseDouble(in[k + 1])) {
                    String temp = in[k];
                    in[k] = in[k + 1];
                    in[k + 1] = temp;
                }
            }
        }

        for (int i = 0; i < in.length - 1; i++) {
            if (Double.parseDouble(in[i]) != Double.parseDouble(in[i + 1])) {
                n++;
            }
        }
        return n;
    }

    double getThreshold(int i) {
        String[] in_attrib = data.getAttributeValues(i);
        String[] out_attrib = data.getAttributeValues(data.getClassIndex());
        for (int j = 0; j < in_attrib.length; j++) {
            for (int k = 0; k < in_attrib.length - 1; k++) {
                if (Double.parseDouble(in_attrib[k]) > Double.parseDouble(in_attrib[k + 1])) {
                    String temp = in_attrib[k];
                    String temp1 = out_attrib[k];
                    in_attrib[k] = in_attrib[k + 1];
                    out_attrib[k] = out_attrib[k + 1];
                    in_attrib[k + 1] = temp;
                    out_attrib[k + 1] = temp1;
                }
            }
        }

        String[] out_values = data.getAttributeRange(data.getClassIndex());

        int N = countDistinct(in_attrib);
        double M1 = 0;
        if (N < 2) {
            return 0;
        } else {
            M1 = Math.log((double) N - 1) / Math.log((double) 2);
            M1 = M1 / data.num_instances;
        }
        int size = data.num_instances;
        double out_info = calculateGain();
        double[] threshold = new double[in_attrib.length];

        int out_size = out_values.length;
        int count = 1;
        double minsplit = 0.10 * size / (out_size);
        int lowitems = 0;
        int highitems = size - lowitems;
        if (minsplit <= 2) {
            minsplit = 2;
        } else if (minsplit > 25) {
            minsplit = 25;
        }

        double t1, t2;
        int tries = 0;
        double threshcost = 0;


        for (int k = 1; k < size - 1; k++) {
            lowitems += 1;
            if (lowitems < minsplit) {
                continue;
            } else if (lowitems > (size - minsplit)) {
                break;
            }

            t1 = Double.parseDouble(in_attrib[k]);
            t2 = Double.parseDouble(in_attrib[k + 1]);
            if (t1 < t2 - 1E-5) {
                //t1 is much smaller than t2
                tries++;
            } else {
                count++;
                continue;
            }
        }
        //high variation in values?
        if (tries > 0) {
            threshcost = (Math.log((double) tries) / (double) size) / Math.log((double) 2);
        } else {
            threshcost = 0;
        }


        int index = 0;
        double max = 0;
        for (int k = 1; k < size - 1; k++) {
            if (in_attrib[k].compareTo(in_attrib[k - 1]) != 0) {
                threshold[k] = calculateThreshold(out_attrib, out_values, k);
                threshold[k] = (out_info - threshold[k] - M1) / splitInfo(k);
                if (threshold[k] > max) {
                    index = k;
                    max = threshold[k];
                }
            } else {
                continue;
            }
        }
        return Double.parseDouble(in_attrib[index].trim());

    }

    /**
     * Calculate threshold values
     * @param out_attrib possible attribute values
     * @param out_values values of instances for an attribute
     * @param j number of instances considered
     * @return 
     */
    double calculateThreshold(String[] out_attrib, String[] out_values, int j) {
        int len = out_attrib.length - j;
        String[] div1 = new String[j];
        String[] div2 = new String[len];

        int l = 0, k = 0;
        for (int i = 0; i < out_attrib.length; i++) {
            if (i < j) {
                //attributes before j
                div1[l] = out_attrib[i];
                l++;
            } else {
                //attributes after j
                div2[k] = out_attrib[i];
                k++;
            }
        }
        double entropy = 0;
        entropy = entropy + ((div1.length / (float) out_attrib.length) * getThreshold(div1, out_values));
        entropy = entropy + ((div2.length / (float) out_attrib.length) * getThreshold(div2, out_values));
        return entropy;
    }

    /**
     * Get threshold for list of instance classes, using possible class values
     * @param out instance class values
     * @param in possible class values
     * @return 
     */
    double getThreshold(String[] out, String[] in) {
        // System.out.println("");
        double threshold = 0.0;
        for (int i = 0; i < in.length; i++) {
            int temp = 0;
            for (int j = 0; j < out.length; j++) {
                if (in[i].trim().compareTo(out[j].trim()) == 0) {
                    temp++;
                }
            }
            //      System.out.println(temp+":"+out.length);
            double tem = temp / (float) out.length;
            if (tem != 0 && out.length != 0 && tem != 1.0) {
                threshold = threshold + (-tem) * (Math.log(tem) / Math.log(2.0));
            } else {
                return 0.0;
            }
        }

        return threshold;

    }

    /**
     * Calculate entropy of a set
     * @param v vector whose entropy is calculated
     * @return entropy
     */
    double getEntropy(Vector v) {        
        String[] outputValues = discreteOutputValues(data.getClassIndex());
        int[] nk = new int[outputValues.length];
        int total = 0;
        for (int i = 0; i < outputValues.length; i++) {
            nk[i] = 0;
            ListIterator lt = v.listIterator();
            for (int j = 0; j < v.size(); j++) {
                String[] temp = (String[]) lt.next();
                if (temp[data.getClassIndex()].trim().compareTo(outputValues[i].trim()) == 0) {
                    nk[i]++;
                }
            }
            total += nk[i];
        }
        double entropy = 0;
        for (int i = 0; i < nk.length; i++) {
            double temp = nk[i] / (float) total;
            if (temp == 0 || total == 0) {
                continue;
            }
            entropy = entropy + (-1 * temp) * (Math.log(temp) / Math.log(2.0));
        }

        return entropy;
    }

    /**
     * Returns possible values of an attribute
     * @param i attribute
     * @return 
     */
    String[] discreteOutputValues(int i) {
        return data.getAttributeRange(i);
    }

    /**
     * Number of instances in the data having value s
     * @param s value compared
     * @param j attribute column
     * @return Instances equal to s
     */
    Vector getPartialData(String s, int j) {
        Vector temp = new Vector();
        for (int i = 0; i < data.num_instances; i++) {
            if (s.compareTo(data.data_array[i][j]) == 0) {
                temp.add(data.data_array[i]);
            }
        }
        return temp;
    }

    /**
     * Number of instances having value s
     * @param s value compared
     * @param j attribute column
     * @return number of instances
     */
    int getPartialDataSize(String s, int j) {
        Vector temp = getPartialData(s, j);
        return temp.size();
    }
}