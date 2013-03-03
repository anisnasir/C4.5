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
    static boolean has(Vector v, String temp) {
        boolean flag = true;
        ListIterator it = v.listIterator();
        while (it.hasNext()) {
            if (it.next().toString().compareTo(temp) == 0) {
                flag = false;
            }
        }
        return flag;


    }

    public static void main(String[] args) {
        // TODO code application logic here
        try {
            BufferedReader reader = new BufferedReader(new FileReader("dataset\\iris.arff"));
            Instances data = new Instances(reader);
            data.convert();
            reader.close();
            
            data.setClassIndex(data.num_attributes - 1);

            System.out.println("num instances:" + data.numInstances());

            C45 dtree=new C45();
            dtree.setData(data);
            String out = dtree.buildTree();
            System.out.println(out);
            
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}