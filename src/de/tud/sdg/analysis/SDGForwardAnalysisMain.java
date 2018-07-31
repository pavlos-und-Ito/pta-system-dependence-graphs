/**
 * SDGForwardAnalysisMain.java
 *
 * @author ifmalcuaz
 * @date 17 Feb 2018
 */
package de.tud.sdg;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;

/**
 * Type description.
 *
 * @author ifmalcuaz
 * @date 17 Feb 2018
 * @see
 */
public class SDGForwardAnalysisMain {
    public static void main(String[] args) {
        PackManager.v().getPack("jtp")
                .add(new Transform("jtp.myTransform", new BodyTransformer() {
                    @Override
                    protected void internalTransform(Body body, String phase,
                            Map options) {
                        new SDGForwardAnalysis(
                        new ExceptionalUnitGraph(body));
                    }
                }));
        soot.Main.main(args);
    }
}
