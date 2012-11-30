/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package c45;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ListIterator;
import java.util.Vector;


/**
 *
 * @author Adi
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    static boolean has(Vector v,String temp)
    {
        boolean flag = true;
        ListIterator it = v.listIterator();
            while(it.hasNext())
                if(it.next().toString().compareTo(temp) == 0)
                    flag = false;
        return flag;


    }
    public static void main(String[] args) {
        // TODO code application logic here
        try{
        BufferedReader reader = new BufferedReader(new FileReader("c:\\dataset\\lhwdata.arff"));
        Instances data = new Instances(reader);
        data.convert();
        reader.close();

        data.setClassIndex(data.num_attributes-1);

        System.out.println("test_instance: "+data.instance(5)+"\n");
        
        System.out.println("num instances:"+data.numInstances());


        Vector v = new Vector();
        for(int i =0;i<data.num_instances;i++)
        {
            reader = new BufferedReader(new FileReader("c:\\dataset\\lhwdata.arff"));
            Instances tempdata = new Instances(reader);
            tempdata.convert();
            reader.close();

            tempdata.setClassIndex(tempdata.num_attributes-1);
            CreateRules cr = new CreateRules();
            String temp = cr.getRules(tempdata,tempdata.instance(i));
            if(has(v,temp) == true)
                v.add(temp);
        }
            ListIterator it = v.listIterator();
            while(it.hasNext())
                System.out.println(it.next());

        }catch(Exception e)
        {
            e.printStackTrace();

        }
    }

}
