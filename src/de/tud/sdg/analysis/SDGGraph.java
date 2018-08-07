import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.G;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.PatchingChain;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.exceptions.ThrowAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;

/**
 * Customized ExceptionalUnitGraph to form an SDG.
 **/
public class SDGGraph extends ExceptionalUnitGraph {

	public SDGGraph(Body body) {

                // Superclass constructs the base graph.
		super(body);
		
                // Now we modify the base graph to add the extra nodes comprising the SDG.
		PatchingChain<Unit> unitsChain = body.getUnits();		
		for(Iterator unitIt = unitsChain.iterator(); unitIt.hasNext();) {
		    Unit unit = (Unit) unitIt.next();
		    
		    for(Iterator boxIt = unit.getUseAndDefBoxes().iterator(); boxIt.hasNext();) {
		        ValueBox box = (ValueBox) boxIt.next();
		        
		        // Method call as head and method dest as tail.
		        if(box.getValue() instanceof MethodOrMethodContext) {
						super.addEdge(super.unitToSuccs, super.unitToPreds, unit, (Unit) unitIt.next());
		        }
		    }
		}
	}

	@Override
	protected void initialize(ThrowAnalysis throwAnalysis,
			boolean omitExceptingUnitEdges) {
		super.initialize(throwAnalysis, omitExceptingUnitEdges);
	}

	@Override
	protected Map<Unit, Collection<ExceptionDest>> buildExceptionDests(
			ThrowAnalysis throwAnalysis) {
		return super.buildExceptionDests(throwAnalysis);
	}

	@Override
	protected Set<Unit> buildExceptionalEdges(ThrowAnalysis throwAnalysis,
			Map<Unit, Collection<ExceptionDest>> unitToExceptionDests,
			Map<Unit, List<Unit>> unitToSuccs,
			Map<Unit, List<Unit>> unitToPreds, boolean omitExceptingUnitEdges) {
		return super.buildExceptionalEdges(throwAnalysis, unitToExceptionDests,
				unitToSuccs, unitToPreds, omitExceptingUnitEdges);
	}

	@Override
	protected void buildHeadsAndTails() throws IllegalStateException {
		super.buildHeadsAndTails();
	}
}
