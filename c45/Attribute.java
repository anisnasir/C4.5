/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package c45;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Paul
 */
public class Attribute {

    String name;
    String type;
    String[] values;

    public Attribute() {
    }

    public Attribute(String arffAttrib) {
        String temp = arffAttrib.toUpperCase();
        name=arffAttrib.substring(11).split("\\s+")[0];        
        if (temp.contains("NUMERIC") || temp.contains("REAL")) {
            type = "REAL";
        } else {
            type="DISCRETE";
            Pattern p = Pattern.compile("\\{(.*)\\}");
            Matcher m = p.matcher(arffAttrib);
            m.find();
            values=m.group(1).split(",\\s*");
        }
    }
}