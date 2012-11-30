/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package c45;

import java.io.BufferedReader;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author Adi
 */
public class Instances {
    String relation;
    Vector attributes;
    Vector data;
    String[][]data_array;
    int num_instances;
    int num_attributes;
    int class_index;
    String prob_name;
    Instances()
    {
        
    }
    Instances(BufferedReader reader){
        num_attributes = 0;
        num_instances  = 0;
        attributes = new Vector();
        data = new Vector();
        String temp;

        try{
            temp = reader.readLine();
        
            while(true)
            {
                if(!reader.ready())
                    break;
                else
                { 
                    {
                        StringTokenizer st = new StringTokenizer(temp," ");
                        if(st.hasMoreTokens())
                        {
                        String temp1 = st.nextToken();
                        if(temp1.compareTo("@relation") == 0)
                        {
                            prob_name = st.nextToken();
                            
                        }else if(temp1.compareTo("@attribute") == 0)
                        {
                            attributes.add(temp);
                            num_attributes++;
                        }else if(temp1.compareTo("@data") == 0)
                        {
                            do{
                                temp = reader.readLine();
                                if(temp != null)
                                {
                                    data.add(temp);
                                    num_instances++;
                                }
                            }while(temp != null);
                            break;
                        }
                        }
                    }

                 temp = reader.readLine();
            }
            }

        }catch(Exception e)
        {
          e.printStackTrace();
        }
    }
    void convert(){
        ListIterator it = data.listIterator();
        data_array = new String[num_instances][num_attributes];
        for(int i=0;i<num_instances;i++)
        {
            String temp = it.next().toString();
            StringTokenizer st = new StringTokenizer(temp,",");
            int j=0;
            while(st.hasMoreTokens())
            {
                String temp1 = st.nextToken().trim();
                if(temp1.compareTo("?") == 0)
                    data_array[i][j] = null;
                else
                    data_array[i][j] = temp1;
                j++;
            }
        }

    }
    int numAttributes()
    {
        return num_attributes;
    }
    int numInstances()
    {
        return num_instances;
    }
    String instance(int j){
        ListIterator it = data.listIterator();
        for(int i =0;i<j;i++)
            it.next();
        return it.next().toString();
    }
    String attribute(int j){
        ListIterator it = attributes.listIterator();
        for(int i =0;i<j;i++)
            it.next();
        return it.next().toString();
    }
    String value(String instance,int i)
    {
        int j=0;
        StringTokenizer st = new StringTokenizer(instance,",");
                        while(st.hasMoreTokens() && j <i)
                        {
                            st.nextToken();
                            j++;
                        }
        if(st.hasMoreTokens())
            return st.nextToken();
        else
            return null;

    }
    String instance_attribute(int i,int j)
    {
        return data_array[i][j];
    }

    void setClassIndex(int i)
    {
        class_index = i;
    }
    int getClassIndex()
    {
        return class_index;
    }
    String[] getAttributeValues(int i)
    {
        String []array =  new String[num_instances];
        for(int j =0;j<num_instances;j++)
            array[j] = instance_attribute(j,i);
        return array;
    }
    int count_values(String[]array,String value)
    {
        int count = 0;
        for (int i =0;i<array.length;i++)
            if(value.compareTo(array[i])== 0 )
                count++;
        return count;
    }
    String attributeType(int i)
    {
        String temp = attributes.get(i).toString();
        if(temp.contains("numeric") || temp.contains("real"))
            return "real";
        else
            return "discrete";
    }
    String[] allAttributeType()
    {
        String []array = new String[num_attributes];
        for (int i =0;i<num_attributes;i++)
            array[i] = attributeType(i);
        return array;
    }
    Vector getData(){
        return data;
    }
    String[] getOutputValues()
    {
        String[]array = new String[num_attributes];
        String[]temp = allAttributeType();
        for(int i =0;i<temp.length;i++)
        {
            String s = attribute(i);
            if(attributeType(i).compareTo("real") == 0 )
                array[i]=null;
            else
            {
                StringTokenizer st = new StringTokenizer(s,"{");
                st.nextToken();
                array[i] = st.nextToken().replace("}", "");
            }
        }
        return array;
    }

}
