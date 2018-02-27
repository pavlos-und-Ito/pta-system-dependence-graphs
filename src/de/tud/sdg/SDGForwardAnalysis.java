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
public class SDGForwardAnalysis {
    protected Map<Unit, List> unitToGuaranteedDefs;

    public SDGForwardAnalysis(UnitGraph graph) {
        if (Options.v().verbose()) {
            G.v().out.println("[" + graph.getBody().getMethod().getName()
                    + "]     Constructing SDG...");
        }

        final GuaranteedDefsAnalysis analysis = new GuaranteedDefsAnalysis(
                graph);

        // build map
        {
            this.unitToGuaranteedDefs = new HashMap<Unit, List>(
                    graph.size() * 2 + 1, 0.7f);
            final Iterator unitIt = graph.iterator();

            while (unitIt.hasNext()) {
                final Unit s = (Unit) unitIt.next();
                final FlowSet set = (FlowSet) analysis.getFlowBefore(s);
                this.unitToGuaranteedDefs.put(s,
                        Collections.unmodifiableList(set.toList()));
            }
        }
    }

    /**
     * Returns a list of locals guaranteed to be defined at (just before)
     * program point <tt>s</tt>.
     **/
    public List getGuaranteedDefs(Unit s) {
        return this.unitToGuaranteedDefs.get(s);
    }
}

/**
 * Flow analysis to determine all locals guaranteed to be defined at a given
 * program point.
 **/
class GuaranteedDefsAnalysis extends ForwardFlowAnalysis {
    FlowSet emptySet = new ArraySparseSet();
    Map<Unit, FlowSet> unitToGenerateSet;

    GuaranteedDefsAnalysis(UnitGraph graph) {
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

    @Override
    protected void copy(Object source, Object dest) {
        final FlowSet sourceSet = (FlowSet) source, destSet = (FlowSet) dest;

        sourceSet.copy(destSet);
    }

    /**
     * IN(Start) is the empty set
     **/
    @Override
    protected Object entryInitialFlow() {
        return this.emptySet.clone();
    }

    /**
     * OUT is the same as IN plus the genSet.
     **/
    @Override
    protected void flowThrough(Object inValue, Object unit, Object outValue) {
        final FlowSet in = (FlowSet) inValue, out = (FlowSet) outValue;

        // perform generation (kill set is empty)
        in.union(this.unitToGenerateSet.get(unit), out);
    }

    /**
     * All paths == Intersection.
     **/
    @Override
    protected void merge(Object in1, Object in2, Object out) {
        final FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2,
                outSet = (FlowSet) out;

        inSet1.intersection(inSet2, outSet);
    }

    /**
     * All INs are initialized to the empty set.
     **/
    @Override
    protected Object newInitialFlow() {
        return this.emptySet.clone();
    }
}
