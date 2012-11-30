/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package c45;

import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;
/**
 *
 * @author Adi
 */
public class CreateRules {
    Instances data;
    String rules;
    CreateRules()
    {
        rules ="";
    }
    String getRules(Instances d,String test)
    {
        data = d;
        
         int n=0;
      while(loopBreak() == false && n< 5)
       {
          n++;
            int i = selectFeature();
            String he = data.attribute(i);
            StringTokenizer st = new StringTokenizer(he," ");
            st.nextToken();
            String value = data.value(test,i);
            
            Vector v = new Vector();
            if(data.attributeType(i).trim().compareTo("discrete") == 0)
            {
                rules = rules+"if ("+st.nextToken()+" == "+value+")";
                for(int j =0;j<data.num_instances;j++)
                    if(data.data_array[j][i].trim().compareTo(value.trim()) == 0)
                        v.add(data.instance(j));
                data.data = v;
                data.num_instances = v.size();
                data.convert();
                rules = rules + " && ";
            }else
            {
                double threshold_value = getThreshold(i);
                double test_value = Double.parseDouble(data.value(test, i));
                if(test_value >= threshold_value)
                {
                    rules = rules+"if ("+st.nextToken()+" >= "+threshold_value+")";
                    for(int j=0;j<data.num_instances;j++)
                    {
                        Double num = Double.parseDouble(data.data_array[j][i]);
                        if(num>=threshold_value)
                            v.add(data.instance(j));
                    }
                }
                else
                {
                    rules = rules+"if ("+st.nextToken()+" < "+threshold_value+")";
                    for(int j=0;j<data.num_instances;j++)
                    {
                        Double num = Double.parseDouble(data.data_array[j][i]);
                        //System.out.println("num:"+num);
                        if(num<threshold_value)
                            v.add(data.instance(j));
                    }
                }
                data.data = v;
                data.num_instances = v.size();
                data.convert();
                rules = rules+" && ";
           }
       } 
        return rules.substring(0, rules.length()-3)+getLeaf();
    }
    boolean loopBreak()
    {
        if(data.num_instances<5)
            return true;
        boolean flag = true;
        String[] temp= data.getAttributeValues(data.class_index);
        if(temp.length == 0)
            return false;
        String test = temp[0];
        for(int i=0;i<temp.length;i++)
            if(test.compareTo(temp[i]) != 0 )
                flag = false;
        return flag;
    }
     String getLeaf()
    {
        return data.data_array[0][data.getClassIndex()];
    }

     double Gain(){
         String []array = data.getAttributeValues(data.getClassIndex());
         String []array1 = discreteOutputValues(data.getClassIndex());
         int [] nk = new int[array1.length];
         for (int i =0;i<array.length;i++)
             for(int j=0;j<array1.length;j++)
                if(array[i].compareTo(array1[j]) == 0)
                    nk[j]++;

        double entropy = 0;
        int total = array.length;
        for(int i=0;i<nk.length;i++)
        {
            double temp = nk[i]/(float)total;
            if(temp == 0 || total == 0)
                continue;
            entropy = entropy + (-1*temp)*(Math.log(temp)/Math.log(2.0));
        }

        return entropy;
     }
    int selectFeature()
    {
        double []array = new double[data.num_attributes-1];
        for (int i =0;i<data.num_attributes-1;i++)
        {
            String[] temp = data.allAttributeType();
            if(temp[i].compareTo("real") == 0)
                array[i]= calculateContinuousEntropy(i);
            else
                array[i] = (Gain()-calculateDiscreteEntropy(i))/splitInfoDiscrete(i);
        }
        int max = 0;
        for (int i=0;i<array.length;i++)
        {
            StringTokenizer st = new StringTokenizer(data.attribute(i)," ");
            st.nextToken();
            //System.out.println(""+st.nextToken().trim()+":"+array[i]);
            if(array[max]<array[i])
                max = i;
        }
        //System.out.println("");
        return max;
    }
    double splitInfoDiscrete(int i)
    {
        String []array = data.getAttributeValues(i);
        String array1= data.attribute(i);
        StringTokenizer st = new StringTokenizer(array1,"{");
        st.nextToken();
        String temp = st.nextToken();
        st = new StringTokenizer(temp,"}");
        temp = st.nextToken();
        st = new StringTokenizer(temp,",");
        int p_values = st.countTokens();
        String []array2 = new String[p_values];
        int k=0;
        while(st.hasMoreTokens())
        {
            array2[k] = st.nextToken();
            array2[k] = array2[k].trim();
            k++;
        }
        int []nk = new int[p_values];
        for(int j=0;j<array2.length;j++)
            for(k=0;k<array.length;k++)
                if(array[k].compareTo(array2[j]) == 0)
                    nk[j]++;
        float info = 0;
        for (int x=0;x<nk.length;x++)
        {
            float tempo = nk[x]/(float)data.num_instances;
            if(tempo != 0 && tempo != 1)
                info += (-1*tempo)*(Math.log(tempo)/Math.log(2.0));
            //System.out.println(info);
        }
        if(info == 0)
            return .000001;
     return info;
    }
    double calculateDiscreteEntropy(int i)
    {
        String []array = discreteOutputValues(i);
        double entropy = 0.0;
        for (int j=0;j<array.length;j++)
        {
            double temp = sizePartialData(array[j],i)/(float)data.num_instances;
            entropy = entropy +  (temp)*getEntropy(partialData(array[j],i));
        }
        return entropy;
    }

    double splitInfo(int i)
    {
        int total = data.num_instances;
        double entropy = 0;
        entropy = entropy + (-1*i/(float)total)*(Math.log(i/(float)total)/Math.log(2.0));
        entropy = entropy + (-1*(total-i)/(float)total)*(Math.log((total-i)/(float)total)/Math.log(2.0));
        return entropy;
    }

    double calculateContinuousEntropy(int i)
    {
        String []in_attrib = data.getAttributeValues(i);
        String []out_attrib = data.getAttributeValues(data.getClassIndex());
        for(int j=0;j<in_attrib.length;j++)
        {
            for(int k=0;k<in_attrib.length-1;k++)
            {
                if(Double.parseDouble(in_attrib[k])>Double.parseDouble(in_attrib[k + 1]))
                {
                    String temp = in_attrib[k];
                    String temp1 = out_attrib[k];
                    in_attrib[k] = in_attrib[k+1];
                    out_attrib[k] = out_attrib[k+1];
                    in_attrib[k+1] = temp;
                    out_attrib[k+1] = temp1;
                }
            }
        }

        String[] out_values = data.getOutputValues();
        String temp11 = out_values[data.getClassIndex()];
        StringTokenizer st = new StringTokenizer(temp11,",");
        int count = st.countTokens();
        out_values = new String[count];

        for (int j=0;j<count;j++)
            out_values[j] = st.nextToken().trim();


        int N = countDistinct(in_attrib);
        double M1=0;
        if(N<2)
            return 0;
        else
        {
	M1= Math.log((double)N-1)/Math.log((double)2);
	M1=M1/data.num_instances;
        }

        int size = data.num_instances;
        double out_info = Gain();
        double []threshold = new double[in_attrib.length];

        int out_size = out_values.length;
        count=1;
	double minsplit=0.10*  size/ (out_size);
	int lowitems=0;
	int highitems = size-lowitems;
	if(minsplit <= 2)
            minsplit=2;
	else if(minsplit > 25)
            minsplit=25;

	double t1,t2;
	int tries=0;
	double threshcost=0;

 
        for(int k=1;k<size-1;k++)
	{
		lowitems +=1;
		if(lowitems < minsplit)
                    continue;
		else if(lowitems > (size-minsplit))
                            break;

		t1=Double.parseDouble(in_attrib[k]);
		t2=Double.parseDouble(in_attrib[k+1]);
		if(t1 < t2 - 1E-5)
		{
		tries++;
		}
		else
		{
			count++;
			continue;
		}
	}
	if(tries>0)
	{
	threshcost= (Math.log((double)tries)/(double) size)/Math.log((double)2);
	}
	else
		threshcost=0;


        double max =0;
        for (int k=1;k<size-1;k++)
        {
            if(in_attrib[k].compareTo(in_attrib[k-1]) != 0)
            {
                threshold[k] = calculateThreshold(out_attrib,out_values,k);
                threshold[k] =  (out_info - threshold[k]-M1)/splitInfo(k);
                threshold[k] =  threshold[k]-threshcost;
                if(threshold[k]>max)
                    max = threshold[k];
            }else
                continue;
        }
        return max;



}
    int countDistinct(String[] in)
    {
        int n =1;
        for(int j=0;j<in.length;j++)
        {
            for(int k=0;k<in.length-1;k++)
            {
                if(Double.parseDouble(in[k])>Double.parseDouble(in[k + 1]))
                {
                    String temp = in[k];
                    in[k] = in[k+1];
                    in[k+1] = temp;
                }
            }
        }
        
        for(int i =0;i<in.length-1;i++)
            if(Double.parseDouble(in[i])!= Double.parseDouble(in[i + 1]))
                n++;
        return n;
    }
    double getThreshold(int i)
    {
         String []in_attrib = data.getAttributeValues(i);
        String []out_attrib = data.getAttributeValues(data.getClassIndex());
        for(int j=0;j<in_attrib.length;j++)
        {
            for(int k=0;k<in_attrib.length-1;k++)
            {
                if(Double.parseDouble(in_attrib[k])>Double.parseDouble(in_attrib[k + 1]))
                {
                    String temp = in_attrib[k];
                    String temp1 = out_attrib[k];
                    in_attrib[k] = in_attrib[k+1];
                    out_attrib[k] = out_attrib[k+1];
                    in_attrib[k+1] = temp;
                    out_attrib[k+1] = temp1;
                }
            }
        }

        String[] out_values = data.getOutputValues();
        String temp11 = out_values[data.getClassIndex()];
        StringTokenizer st = new StringTokenizer(temp11,",");
        int count = st.countTokens();
        out_values = new String[count];

        for (int j=0;j<count;j++)
            out_values[j] = st.nextToken().trim();


        int N = countDistinct(in_attrib);
        double M1=0;
        if(N<2)
            return 0;
        else
        {
	M1= Math.log((double)N-1)/Math.log((double)2);
	M1=M1/data.num_instances;
        }
int size = data.num_instances;
        double out_info = Gain();
        double []threshold = new double[in_attrib.length];

        int out_size = out_values.length;
        count=1;
	double minsplit=0.10*  size/ (out_size);
	int lowitems=0;
	int highitems = size-lowitems;
	if(minsplit <= 2)
            minsplit=2;
	else if(minsplit > 25)
            minsplit=25;

	double t1,t2;
	int tries=0;
	double threshcost=0;


        for(int k=1;k<size-1;k++)
	{
		lowitems +=1;
		if(lowitems < minsplit)
                    continue;
		else if(lowitems > (size-minsplit))
                            break;

		t1=Double.parseDouble(in_attrib[k]);
		t2=Double.parseDouble(in_attrib[k+1]);
		if(t1 < t2 - 1E-5)
		{
		tries++;
		}
		else
		{
			count++;
			continue;
		}
	}
	if(tries>0)
	{
	threshcost= (Math.log((double)tries)/(double) size)/Math.log((double)2);
	}
	else
		threshcost=0;


        int index =0;
        double max =0;
        for (int k=1;k<size-1;k++)
        {
            if(in_attrib[k].compareTo(in_attrib[k-1]) != 0)
            {
                threshold[k] = calculateThreshold(out_attrib,out_values,k);
                threshold[k] =  (out_info - threshold[k]-M1)/splitInfo(k);
                if(threshold[k]>max)
                {
                    index = k;
                    max = threshold[k];
                }
            }
            else
                continue;
        }
        return Double.parseDouble(in_attrib[index].trim());

 }


  
    double calculateThreshold(String [] out_attrib,String []out_values,int j)
    {
        int len = out_attrib.length-j;
        String[] div1 = new String[j];
        String[] div2 = new String[len];

        int l=0,k=0;
        for (int i =0; i< out_attrib.length;i++)
        {
            if(i<j)
            {
                div1[l] = out_attrib[i];
                l++;
            }
            else
            {
                div2[k] = out_attrib[i];
                k++;
            }
        }
        double entropy =0;
        entropy = entropy+((div1.length/(float)out_attrib.length)*getThreshold(div1,out_values));
        entropy = entropy+((div2.length/(float)out_attrib.length)*getThreshold(div2,out_values));
        return entropy;
    }
    double getThreshold(String[] out,String []in)
    {
        // System.out.println("");
        double threshold = 0.0;
        for (int i=0;i<in.length;i++)
        {
            int temp =0;
            for(int j=0;j<out.length;j++)
            {
                if(in[i].trim().compareTo(out[j].trim()) == 0)
                    temp++;
            }
      //      System.out.println(temp+":"+out.length);
            double tem = temp/(float)out.length;
            if(tem != 0 && out.length !=0 && tem!=1.0)
                threshold = threshold + (-tem)*(Math.log(tem)/Math.log(2.0));
            else
                return 0.0;
        }

        return threshold;

    }
    double getEntropy(Vector v)
    {
        String[]array = discreteOutputValues(data.getClassIndex());
        int[] nk = new int[array.length];
        int total = 0;
        for(int i =0;i<array.length;i++)
        {
            nk[i] = 0;
            ListIterator lt = v.listIterator();
            for(int j=0;j<v.size();j++)
            {
                String temp = lt.next().toString();
                if(data.value(temp, data.getClassIndex()).trim().compareTo(array[i].trim()) == 0)
                    nk[i]++;
            }
            total += nk[i];
        }
        double entropy = 0;
        for(int i=0;i<nk.length;i++)
        {
            double temp = nk[i]/(float)total;
            if(temp == 0 || total == 0)
                continue;
            entropy = entropy + (-1*temp)*(Math.log(temp)/Math.log(2.0));
        }

        return entropy;
    }
    String[] discreteOutputValues(int i)
    {
        String []array = data.getOutputValues();
        String temp = array[i];
        StringTokenizer st = new StringTokenizer(temp,",");
        int count = st.countTokens();
        array = new String[count];
        for (int j=0;j<count;j++)
        {
            array[j] = st.nextToken();
            array[j] = array[j].trim();
        }
        return array;
    }
    Vector partialData(String s, int j)
    {
        Vector temp = new Vector();
        for(int i =0 ; i<data.num_instances;i++)
        {
            if(s.compareTo(data.value(data.instance(i),j)) == 0)
                temp.add(data.instance(i));
        }
        return temp;
    }
    int sizePartialData(String s, int j)
    {
        Vector temp = partialData(s,j);
        return temp.size();
    }


}
