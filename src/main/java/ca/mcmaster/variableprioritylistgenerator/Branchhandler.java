/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.variableprioritylistgenerator;

import static ca.mcmaster.variableprioritylistgenerator.Constants.*;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 *
 * @author tamvadss
 */
public class Branchhandler extends IloCplex.BranchCallback{
 
    protected void main() throws IloException {
        //
        if ( getNbranches()> ZERO ){ 
            //get the branches about to be created
            IloNumVar[][] vars = new IloNumVar[TWO][] ;
            double[ ][] bounds = new double[TWO ][];
            IloCplex.BranchDirection[ ][] dirs = new IloCplex.BranchDirection[ TWO][];
            getBranches( vars, bounds, dirs);

            IloNumVar var = vars[ZERO][ZERO];
            String varName= var.getName();

            double up_PS = getUpPseudoCost(var);
            double down_PS = getDownPseudoCost(var);
            double max_PS = Math.max(up_PS, down_PS);
            double min_PS = Math.min(up_PS, down_PS);
            ListGenerator.downPseudoCostMap.put(varName, down_PS);
            ListGenerator.upPseudoCostMap.put(varName,up_PS );
            ListGenerator.cumulativePseudoCostMap.put(varName,ALPHA * max_PS + (ONE-ALPHA)*min_PS);
        }
        
    }
    
}
