/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package c45;

/**
 *
 * @author Paul
 */
public class Condition {

    static int EQ=0;
    static int LT=1;
    static int GTE=2;
    String name;
    int column;
    int condition;
    String value;

    public Condition(String name, int column, int condition, String value) {
        this.name = name;
        this.column = column;
        this.condition = condition;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Condition) {
            
            Condition temp = (Condition) obj;
            boolean result =(this.name.equals(temp.name) && this.value.equals(temp.value)
                    && this.column == temp.column && this.condition == condition);
            return result;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode()+column+condition+value.hashCode();
    }
    

    @Override
    public String toString() {
        return "["+name+"("+column+") <"+condition+"> "+value+"]";
    }
    
}
