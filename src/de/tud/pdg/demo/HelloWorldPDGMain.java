/**
 * HelloWorldPDGMain.java
 *
 * @author ifmalcuaz
 * @date 16 Feb 2018
 */
package de.tud.pdg.demo;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.jimple.toolkits.pointer.LocalMustNotAliasAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;

/**
 * Type description.
 *
 * @author ifmalcuaz
 * @date 16 Feb 2018
 * @see
 */
public class HelloWorldPDGMain {
    public static void main(String[] args) {
        PackManager.v().getPack("jtp")
                .add(new Transform("jtp.myTransform", new BodyTransformer() {
                    @Override
                    protected void internalTransform(Body body, String phase,
                            Map options) {
                        new LocalMustNotAliasAnalysis(
                                new ExceptionalUnitGraph(body));
                    }
                }));
        soot.Main.main(args);
    }
}
