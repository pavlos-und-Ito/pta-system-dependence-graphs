/**
 * SDGForwardAnalysis.java
 *
 * @author ifmalcuaz
 * @date 17 Feb 2018
 */
package de.tud.sdg;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.G;
import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.options.Options;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/**
 * Type description.
 *
 * @author ifmalcuaz
 * @date 17 Feb 2018
 * @see
 */
class SDGForwardAnalysis extends ForwardFlowAnalysis {
    FlowSet emptySet = new ArraySparseSet();
    Map<Unit, FlowSet> unitToGenerateSet;

    SDGForwardAnalysis(UnitGraph graph) {
        super(graph);
        final DominatorsFinder df = new MHGDominatorsFinder(graph);
        this.unitToGenerateSet = new HashMap<Unit, FlowSet>(
                graph.size() * 2 + 1, 0.7f);

        // pre-compute generate sets
        for (final Object element : graph) {
            final Unit s = (Unit) element;
            final FlowSet genSet = this.emptySet.clone();

            for (final Iterator domsIt = df.getDominators(s).iterator(); domsIt
                    .hasNext();) {
                final Unit dom = (Unit) domsIt.next();
                for (final Object element2 : dom.getDefBoxes()) {
                    final ValueBox box = (ValueBox) element2;
                    if (box.getValue() instanceof Local) {
                        genSet.add(box.getValue(), genSet);
                    }
                }
            }

            this.unitToGenerateSet.put(s, genSet);
        }

        doAnalysis();
    }

    /**
     * All INs are initialized to the empty set.
     **/
    protected Object newInitialFlow()
    {
    	
    	// New sdg out branch
    	if (!unitToGenerateSet.isEmpty()) {
    		emptySet.clear();
    		Map<Unit, FlowSet> newFunctionSet = new HashMap<Unit, FlowSet>(graph.size() * 2 + 1, 0.7f);
    		return newFunctionSet;
    	}
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
        in.union(unitToGenerateSet.get(unit), out);
        
        // sdg branch
        in.clone();
        in.intersection(in, out);
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
        
        // sdg branch
        inSet2.intersection(inSet1, outSet);
    }

    protected void copy(Object source, Object dest)
    {
        FlowSet
            sourceSet = (FlowSet) source,
            destSet = (FlowSet) dest;

        sourceSet.copy(destSet);
        
        // Sdg branch
        ((ArraySparseSet) unitToGenerateSet).copy(destSet);
    }
}
