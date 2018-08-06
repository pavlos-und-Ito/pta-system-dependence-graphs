import java.util.*;

import soot.*;
import soot.options.*;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/**
 * Flow analysis to generate the flowsets from an SDG.
 **/
public class SDGForwardAnalysis extends ForwardFlowAnalysis
{
    FlowSet emptySet = new ArraySparseSet();
    Map<Unit, FlowSet> functionCallToFlowset;

    SDGForwardAnalysis(UnitGraph graph)
    {
        super(graph);
        DominatorsFinder df = new MHGDominatorsFinder(graph);
        functionCallToFlowset = new HashMap<Unit, FlowSet>(graph.size() * 2 + 1, 0.7f);

        for(Iterator unitIt = graph.iterator(); unitIt.hasNext();){
            Unit s = (Unit) unitIt.next();
            FlowSet genSet = emptySet.clone();
            
            for(Iterator domsIt = df.getDominators(s).iterator(); domsIt.hasNext();){
                Unit dom = (Unit) domsIt.next();
                for(Iterator boxIt = dom.getDefBoxes().iterator(); boxIt.hasNext();){
                    ValueBox box = (ValueBox) boxIt.next();
                    
                    // Flowset belonging to a function call.
                    if(box.getValue() instanceof MethodOrMethodContext) {
                        genSet.add(box.getValue(), genSet);
                    }
                }
            }
            
            functionCallToFlowset.put(s, genSet);
        }

        doAnalysis();
    }

    /**
     * All INs are initialized to the empty set.
     **/
    protected Object newInitialFlow()
    {
        return emptySet.clone();
    }

    /**
     * IN(Start) is the empty set
     **/
    protected Object entryInitialFlow()
    {
        return emptySet.clone();
    }

    /**
     * OUT is the same as IN plus the genSet.
     **/
    protected void flowThrough(Object inValue, Object unit, Object outValue)
    {
        FlowSet
            in = (FlowSet) inValue,
            out = (FlowSet) outValue;

        // perform generation (kill set is empty)
        in.union(functionCallToFlowset.get(unit), out);
    }

    /**
     * All paths == Intersection.
     **/
    protected void merge(Object in1, Object in2, Object out)
    {
        FlowSet
            inSet1 = (FlowSet) in1,
            inSet2 = (FlowSet) in2,
            outSet = (FlowSet) out;

        inSet1.intersection(inSet2, outSet);
    }

    protected void copy(Object source, Object dest)
    {
        FlowSet
            sourceSet = (FlowSet) source,
            destSet = (FlowSet) dest;

        sourceSet.copy(destSet);
    }
}
