/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.variableprioritylistgenerator;

import static ca.mcmaster.variableprioritylistgenerator.Constants.*;
import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author tamvadss
 */
public class ListGenerator {
    
    public static Map  <String ,Double> upPseudoCostMap = new HashMap <String ,Double> ();
    public static Map  <String ,Double> downPseudoCostMap = new HashMap <String ,Double> ();
    public static Map  <String ,Double> cumulativePseudoCostMap = new HashMap <String ,Double> ();
    //largest integer is highest priority
    public static Map  <Double, List<String>> variablePriorityMap = new TreeMap < Double, List<String>> ();
    public static Map<String, IloNumVar> allVariablesMap  = new HashMap<String, IloNumVar>();
    
    public static void main(String[] args) throws Exception {
        IloCplex cplex = new IloCplex ();
                
        cplex.importModel(  MIP_FILENAME);
        
        allVariablesMap =getVariables (  cplex) ;
        
        cplex.use (new  Branchhandler());
        

        //use strong branching ?
        //cplex.setParam( IloCplex.Param.MIP.Strategy.VariableSelect  ,  TWO);
        //cplex.setParam( IloCplex.Param.MIP.Limits.StrongCand  , BILLION );
        //cplex.setParam( IloCplex.Param.MIP.Limits.StrongIt ,  BILLION );
        
        cplex.setParam( IloCplex.Param.MIP.Strategy.VariableSelect  ,  TWO);
        cplex.setParam( IloCplex.Param.Emphasis.MIP , MIP_EMPHASIS );
        //node file compressed to disk
        cplex.setParam( IloCplex.Param.MIP.Strategy.File , THREE);
        
        cplex.setParam( IloCplex.Param.MIP.Strategy.HeuristicFreq , -ONE);
        
        
        
        //cplex.setParam( IloCplex.Param.MIP.Strategy.PresolveNode , -ONE  );
        //cplex.setParam( IloCplex.Param.Preprocessing.Presolve,  false);
        
        cplex.setParam( IloCplex.Param.TimeLimit, TIME_LIMIT_MINUTES*SIXTY);
         
        cplex.solve ();
        cplex.end();
                
        for (Map.Entry <String ,Double> entry  : cumulativePseudoCostMap.entrySet()){
            List<String> current = variablePriorityMap.get( entry.getValue());
            if (current == null) current = new ArrayList<String> ();
            current.add (entry.getKey()) ; 
            variablePriorityMap.put( entry.getValue(), current);
        }
        
        for (Map.Entry  <Double, List<String>> entry : variablePriorityMap.entrySet()){
            System.out.println(entry.getKey()) ;    
            for (String name : entry.getValue()){
                 System.out.print(name + ", ") ;    
            }
            System.out.println() ;
        }
        
        savePriorityListToDisk();
        
    }
    
    private static void savePriorityListToDisk () throws Exception {
        Map  <Integer, List<String>> priorityMap = new TreeMap < Integer, List<String>> ();
        int priority = ONE;
        for (Map.Entry  <Double, List<String>> entry : variablePriorityMap.entrySet()){
            priorityMap.put (priority, entry.getValue()) ;
            priority ++;
        }
        
        for (Map.Entry  <Integer, List<String>> entry : priorityMap.entrySet()){
            System.out.println(entry.getKey()) ;
            for (String str : entry.getValue()){
                System.out.print(str+", ") ;
            }
            System.out.println() ;
        }
        
        FileOutputStream fos =                     new FileOutputStream(PRIORITY_LIST_FILENAME);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(priorityMap);
        oos.close();
        fos.close();
        
        FileInputStream fis = new FileInputStream(PRIORITY_LIST_FILENAME);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Map  <Integer, List<String>> recreatedPriorityMap  = (TreeMap) ois.readObject();
        ois.close();
        fis.close();
        
        System.out.println() ;
        System.out.println("Printing recreated Map") ;
        for (Map.Entry  <Integer, List<String>> entry : recreatedPriorityMap.entrySet()){
            System.out.println(entry.getKey()) ;
            for (String str : entry.getValue()){
                System.out.print(str+", ") ;
            }
            System.out.println() ;
        }
        
    }
            
    private static Map<String, IloNumVar> getVariables (IloCplex cplex) throws IloException{
        Map<String, IloNumVar> result = new HashMap<String, IloNumVar>();
        IloLPMatrix lpMatrix = (IloLPMatrix)cplex.LPMatrixIterator().next();
        IloNumVar[] variables  =lpMatrix.getNumVars();
        for (IloNumVar var :variables){
            result.put(var.getName(),var ) ;
        }
        return result;
    }
    
}
