/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package c45;

import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 *
 * @author Paul
 */
public class Rule {

    LinkedHashSet<Condition> conditions;
    String label;

    public Rule() {
        conditions = new LinkedHashSet<Condition>();
    }

    void addCondition(Condition c) {
        conditions.add(c);
    }    

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rule) {        
            boolean result=conditions.containsAll(((Rule) obj).conditions)&&label.equals(((Rule)obj).label);
            return result;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuffer rule=new StringBuffer();
	boolean first=true;
	rule.append("IF ");
        for(Condition c:conditions){
	    if(!first){
		rule.append(" AND ");
	    }else {
		first=false;
	    }
            rule.append(c);
        }
        rule.append(" THEN "+label);
        return rule.toString();
    }    
}
