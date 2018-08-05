import java.util.Iterator;

import soot.Body;
import soot.G;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.PatchingChain;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.ExceptionalUnitGraph;

/**
 * Customized ExceptionalUnitGraph to form an SDG.
 **/
public class SDGGraph extends ExceptionalUnitGraph {

	public SDGGraph(Body body) {
		super(body);
		
		PatchingChain<Unit> unitsChain = body.getUnits();
		
        for(Iterator unitIt = unitsChain.iterator(); unitIt.hasNext();){
            Unit unit = (Unit) unitIt.next();
            
            unit.removeAllTags();
            
            for(Iterator boxIt = unit.getUseAndDefBoxes().iterator(); boxIt.hasNext();){
                ValueBox box = (ValueBox) boxIt.next();
                
                if(box.getValue() instanceof MethodOrMethodContext) {
					G.v().out.println(box.toString());
                    System.out.println(box.toString());
                }
            }
        }
	}
}
