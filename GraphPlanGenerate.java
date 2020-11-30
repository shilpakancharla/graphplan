package q4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class that generates the graph plan and displays the literals and associated mutexes at each state and action layer.
 */

public class GraphPlanGenerate {
    //Initial state held here
    static HashSet<String> initialState = new HashSet<>(); 
    //Goal state held here
    static HashSet<String> goalState = new HashSet<>(); 
    //Contains the action, and associated preconditions and effects
    static HashSet<Action> actions = new HashSet<>(); 

    //ArrayList of a map of the action nodes
    static ArrayList<HashMap<String, ActionNode>> actionNodesMap = new ArrayList<HashMap<String, ActionNode>>();
    //ArrayList of a map of the state nodes
    static ArrayList<HashMap<String, StateNode>> stateNodesMap = new ArrayList<HashMap<String, StateNode>>();
    //Contains information about the state/literal mutexes
    static ArrayList<HashSet<String>> stateMutex = new ArrayList<HashSet<String>>();
    //Contains information about the action mutexes
    static ArrayList<HashSet<String>> actionMutex = new ArrayList<HashSet<String>>();
    //Contains the solution to the planning graph
    static ArrayList<HashSet<String>> extraction = new ArrayList<HashSet<String>>();
    
    //Flag for if a solution is found
    static boolean solutionFlag = false; 

    /**
     * Processes the input from the provided text files. This will populate the initialState, goalState, and actions instantiated above.
     * 
     * @ param fileReader: we pass in a BufferedReader object that allows us to read the file
     * @ param inputFile: the text file where we get our information from
     */
    public static void processFile(BufferedReader fileReader, File inputFile) {
        try {
            //Use InputStreamReader here because you are taking an input fromt he user typing in the console
            //Note: FileReader is used when reading input from a file, will be used later
            String line = "";
            String actionName = "";
            String[] preconditions = new String[0];
            String[] effects = new String[0];
            fileReader = new BufferedReader(new FileReader(inputFile));
            Pattern p = Pattern.compile("\\[[^\\[]*\\]");
            while ((line = fileReader.readLine()) != null) {
                if (line.startsWith("InitialState")) {
                    Matcher m = p.matcher(line);
                    String[] initialStateString = new String[0];
                    while (m.find()) {
                        initialStateString = m.group(0).split(",");
                    }
                    for (int i = 0; i < initialStateString.length; i++) {
                        if (initialStateString[i].contains("[") || initialStateString[i].contains("]") || initialStateString[i].contains("+")) {
                            initialStateString[i] = initialStateString[i].replace("[", "");
                            initialStateString[i] = initialStateString[i].replace("]", "");
                            //Remove '+' from in front of non-negated literals because this makes it easier to compare strings later on without having to reference substrings.
                            initialStateString[i] = initialStateString[i].replace("+", "");
                        }
                    }
                    for (String s: initialStateString) {
                        initialState.add(s);
                    }
                }
                if (line.startsWith("GoalState")) {
                    Matcher m = p.matcher(line);
                    String[] goalStateString = new String[0];
                    while (m.find()) {
                        goalStateString = m.group(0).split(",");
                    }
                    for (int i = 0; i < goalStateString.length; i++) {
                        if (goalStateString[i].contains("[") || goalStateString[i].contains("]") || goalStateString[i].contains("+")) {
                            goalStateString[i] = goalStateString[i].replace("[", "");
                            goalStateString[i] = goalStateString[i].replace("]", "");
                            //Remove '+' from in front of non-negated literals because this makes it easier to compare strings later on without having to reference substrings.
                            goalStateString[i] = goalStateString[i].replace("+", "");
                        }
                    }
                    for (String s: goalStateString) {
                        goalState.add(s);
                    }
                }
                if (line.startsWith("Act")) {
                    if (!actionName.equals("")) {
                        //Instantiate new Action object
                        actions.add(new Action(actionName, preconditions, effects));
                    }
                    Matcher m = p.matcher(line);
                    while (m.find()) {
                        actionName = m.group(0);
                    }
                    actionName = actionName.replace("[", "");
                    actionName = actionName.replace("]", "");
                }
                if (line.startsWith("Precondition")) {
                    Matcher m = p.matcher(line);
                    while (m.find()) {
                        preconditions = m.group(0).split(",");
                    }
                    for (int i = 0; i < preconditions.length; i++) {
                        if (preconditions[i].contains("[") || preconditions[i].contains("]") || preconditions[i].contains("+")) {
                            preconditions[i] = preconditions[i].replace("[", "");
                            preconditions[i] = preconditions[i].replace("]", "");
                            //Remove '+' from in front of non-negated literals because this makes it easier to compare strings later on without having to reference substrings.
                            preconditions[i] = preconditions[i].replace("+", "");
                        }
                    }
                }
                if (line.startsWith("Effect")) {
                    Matcher m = p.matcher(line);
                    while (m.find()) {
                        effects = m.group(0).split(",");
                    }
                    for (int i = 0; i < effects.length; i++) {
                        if (effects[i].contains("[") || effects[i].contains("]") || effects[i].contains("+")) {
                            effects[i] = effects[i].replace("[", "");
                            effects[i] = effects[i].replace("]", "");
                            //Remove '+' from in front of non-negated literals because this makes it easier to compare strings later on without having to reference substrings.
                            effects[i] = effects[i].replace("+", "");
                        }
                    }
                }
            }
            if (!actionName.equals("")) {
                //Instantiate new Action object
                actions.add(new Action(actionName, preconditions, effects));
            }
            fileReader.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        //Adding all possible literals to initial state in case they're not already present
        for (String s: goalState) {
            if (s.startsWith("-")) {
                if (!initialState.contains(s.substring(1))) {
                    initialState.add("-" + s.substring(1));
                }
            } else {
                if (!initialState.contains(s) && !s.contains("-")) {
                    initialState.add("-" + s);
                }
            }
        }
    }

    /**
     * This method allows us to expand the planning graph by its state and action layers. We also use this method to find the different types of mutexes present at each level
     * and we write to our output file the literals, actions, and mutexes present in each state and action layer while we loop through the expansion.
     * 
     * @ param fileWriter: BufferedWriter object we use to write to our output file
     * @ param outputFile: the file we write our results to
     */
    public static void expand(BufferedWriter fileWriter, File outputFile) {
        try {
            fileWriter = new BufferedWriter(new FileWriter(outputFile));
            int layer = 0; //Start with 0 because this makes it easy to reference ArrayList indices
            HashMap<String, StateNode> initMap = new HashMap<String, StateNode>();
            for (String s: initialState) {
                initMap.put(s, new StateNode(s));
            }
            stateNodesMap.add(initMap);
            stateMutex.add(new HashSet<String>());
            boolean soln = solutionFlag;
    
            while (!soln) {
                actionNodesMap.add(new HashMap<String, ActionNode>()); //New action layer instantiated
                stateNodesMap.add(new HashMap<String, StateNode>()); //New state layer instantiated
    
                //Step 1: Look for proper actions and make connections
                for (Action a: actions) {
                    boolean actFlag = true;
                    for (int i = 0; i < a.preconditions.length && actFlag; i++) {
                        //Check if previous states contain this precondition
                        //On the first layer, we would get the first node in stateNodesMap, checks to see if this HashMap in the ArrayList
                        //contains the precondition in the list of preconditions for the specific Action we are looking at
                        if (stateNodesMap.get(layer).containsKey(a.preconditions[i])) {
                            //Check to see if it is mutex with previous preconditions
                            if (layer != 0) {
                                for (int j = 0; j < i && actFlag; j++) {
                                    if (stateMutex.get(layer).contains(a.preconditions[i] + a.preconditions[j]) || 
                                            stateMutex.get(layer).contains(a.preconditions[j] + a.preconditions[i])) {
                                        actFlag = false;
                                    }
                                }
                            }
                        } else { //Jump down here when the precondition is not present
                            actFlag = false;
                        }
                    }
                    if (actFlag) { //Make appropriate connections
                        ActionNode aNode = new ActionNode(a.actionName); //Instantiate new action node object
                        actionNodesMap.get(layer).put(a.actionName, aNode); //Add the string and value to the layer HashMap
                        stateNodesMap.add(new HashMap<>()); //Add another state to the ArrayList of HashMaps which contain the states at each layer
    
                        //Connections between preconditions and actions
                        for (String pName: a.preconditions) {
                            StateNode pState = stateNodesMap.get(layer).get(pName); //Setting the StateNode to a precondition
                            pState.afterState.add(aNode); //Link the action after the precondition
                            aNode.beforeState.add(pState); //Linke the precondition before the action
                        }
    
                        //Connections between actions and effects - effects occur in the next layer and are observable there
                        for (String eName: a.effects) {
                            StateNode eState;
                            //If such a StateNode with the effect has not been created yet, instantiate the StateNode and add the effect
                            if (!stateNodesMap.get(layer + 1).containsKey(eName)) { 
                                eState = new StateNode(eName); //Instantiation
                                stateNodesMap.get(layer + 1).put(eName, eState); //Create the HashMap entry
                            } else { //If such an effect already exists in the StateNode, set the connection
                                eState = stateNodesMap.get(layer + 1).get(eName);
                            }
                            aNode.afterState.add(eState); //Link the effect after the action
                            eState.beforeState.add(aNode); //Link the state before the action
                        }
                    }
                }
                
                //Step 2: Persistent expanding and make connections
                for (String persistentState: stateNodesMap.get(layer).keySet()) {
                    //persistentState contains all the literals that are possible at a particular state, but not necessarily true at that state.
                    //Get effect state node
                    StateNode eState;
                    //The effects were created above, no check to see if that literal exists in the next layer which contains the effects
                    if (!stateNodesMap.get(layer + 1).containsKey(persistentState)) {
                        eState = new StateNode(persistentState); //Create a new state with that literal
                    } else {
                        eState = stateNodesMap.get(layer + 1).get(persistentState); //If it already exists, no need to expand
                    }
    
                    //Create new persistent action node
                    ActionNode actionPersistentNode = new ActionNode("persist[" + persistentState + "]");
                    //Add connections
                    stateNodesMap.get(layer).get(persistentState).afterState.add(actionPersistentNode);
                    actionPersistentNode.beforeState.add(stateNodesMap.get(layer).get(persistentState));
                    actionPersistentNode.afterState.add(eState);
                    eState.beforeState.add(actionPersistentNode);
    
                    stateNodesMap.get(layer + 1).put(eState.name, eState);
                    actionNodesMap.get(layer).put("persist[" + persistentState + "]", actionPersistentNode);
                }
    
                //Step 3: Add action mutexes
                actionMutex.add(new HashSet<String>());
                HashSet<String> competingNeedsMutex = new HashSet<>();
                HashSet<String> inconsistentEffectsMutex = new HashSet<>();
                HashSet<String> interferenceMutex = new HashSet<>();
                
                for (ActionNode a1: actionNodesMap.get(layer).values()) {
                    for (ActionNode a2: actionNodesMap.get(layer).values()) {
                        if (a1.name.equals(a2.name)) continue;
                        //Competing Needs
                        competingNeedsMutex = getCompetingNeeds(a1, a2, competingNeedsMutex, layer);
                        //Inconsistent Effects
                        inconsistentEffectsMutex = getInconsistentEffects(a1, a2, inconsistentEffectsMutex, layer);
                        //Interference
                        interferenceMutex = getInterference(a1, a2, interferenceMutex, layer);
                    }
                }
    
                //Step 4: Add literal mutexes
                stateMutex.add(new HashSet<String>());
                HashSet<String> negatedLiteralsMutex = new HashSet<>();
                HashSet<String> inconsistentSupportMutex = new HashSet<>();
    
                for (StateNode s1: stateNodesMap.get(layer + 1).values()) {
                    for (StateNode s2: stateNodesMap.get(layer + 1).values()) {
                        //Negated Literals
                        if (s1.name.substring(1).equals(s2.name)) continue;
                        if (s1.name.substring(1, s1.name.length()).equals(s2.name) || s2.name.substring(1, s2.name.length()).equals(s1.name)) {
                            negatedLiteralsMutex = getNegatedLiterals(s1, s2, negatedLiteralsMutex, layer);
                        //Inconsistent Support
                        } else { 
                            inconsistentSupportMutex = getInconsistentSupport(s1, s2, inconsistentSupportMutex, layer);
                        }
                    }
                }
    
                //Step 5: Extract
                boolean extractFlag = isTimeToExtract(layer);
    
                //If the time has come to extract a solution, we go through this loop and call to the extract() method.
                if (extractFlag) {
                    int i = 0;
                    while (i < layer + 1) {
                        extraction.add(new HashSet<String>());
                        i++;
                    }
                    extract(layer + 1, goalState, new HashSet<String>());
                    if (solutionFlag) {
                        soln = true;
                    }
                }
                //Update and write results to file along each iteration of the while loop (expansion of planning graph)
                fileWriter.write("StateLayer:\t" + layer + "\n");
                fileWriter.write("\tLiterals:\t");
                for (StateNode s: stateNodesMap.get(layer).values()) {
                    fileWriter.write(s.name + ", ");
                }
                fileWriter.write("\n");
                fileWriter.write("\tNegated Literals:\t");
                for (String a: negatedLiteralsMutex) {
                    fileWriter.write("(" + a + "), ");
                }
                fileWriter.write("\n");
                fileWriter.write("\tInconsistent Support:\t");
                for (String b: inconsistentSupportMutex) {
                    fileWriter.write("(" + b + "), ");
                }
                fileWriter.write("\n");
                fileWriter.write("ActLayer:\t" + layer + "\n");
                fileWriter.write("\tActions:\t");
                for (ActionNode a: actionNodesMap.get(layer).values()) {
                    if (!a.name.contains("persist[")) {
                        fileWriter.write(a.name + ", ");
                    }
                }
                fileWriter.write("\n");
                fileWriter.write("\tInconsistent Effects:\t");
                for (String s: inconsistentEffectsMutex) {
                    if (!s.contains("persist[")) { //Do not print out all the literals at every level
                        fileWriter.write("(" + s + "), ");
                    }
                }
                fileWriter.write("\n");
                fileWriter.write("\tInterference:\t");
                for (String t: interferenceMutex) {
                    if (!t.contains("persist[")) { //Do not print out all the literals at every level
                        fileWriter.write("(" + t + "), ");
                    }
                }
                fileWriter.write("\n");
                fileWriter.write("\tCompeting Needs:\t");
                for (String u: competingNeedsMutex) {
                    if (!u.contains("persist[")) { //Do not print out all the literals at every level
                        fileWriter.write("(" + u + "), ");
                    }
                }
                fileWriter.write("\n");
                //Go to the next layer
                layer++;
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (Exception e2) {
                System.out.println("Error closing the BufferedWriter Object." + e2);
            }
        }
    }

    public static HashSet<String> getCompetingNeeds(ActionNode a1, ActionNode a2, HashSet<String> competingNeedsMutex, int layer) {
        for (StateNode p1: a1.beforeState) {
            for (StateNode p2: a2.beforeState) {
                if (stateMutex.get(layer).contains(p1.name + p2.name)) {
                    actionMutex.get(layer).add(a1.name + a2.name);
                    if (!competingNeedsMutex.contains(a2.name + ", " + a1.name)) {
                        competingNeedsMutex.add(a1.name + ", " + a2.name);
                    }
                }
            }
        }
        return competingNeedsMutex;
    }

    public static HashSet<String> getInconsistentEffects(ActionNode a1, ActionNode a2, HashSet<String> inconsistentEffectsMutex, int layer) {
        for (StateNode e1: a1.afterState) {
            for (StateNode e2: a2.afterState) {
                if (e1.name.substring(1).equals(e2.name) || e2.name.substring(1).equals(e1.name)) {
                    actionMutex.get(layer).add(a1.name + a2.name);
                    if (!inconsistentEffectsMutex.contains(a2.name + ", " + a1.name)) {
                        inconsistentEffectsMutex.add(a1.name + ", " + a2.name);
                    }
                }
            }
        }
        return inconsistentEffectsMutex;
    }

    public static HashSet<String> getInterference(ActionNode a1, ActionNode a2, HashSet<String> interferenceMutex, int layer) {
        for (StateNode p1: a1.beforeState) {
            for (StateNode e2: a2.afterState) {
                if (p1.name.substring(1).equals(e2.name) || e2.name.substring(1).equals(p1.name)) {
                    actionMutex.get(layer).add(a1.name + a2.name);
                    if (!interferenceMutex.contains(a2.name + ", " + a1.name)) {
                        interferenceMutex.add(a1.name + ", " + a2.name);
                    }
                }
            }
        }
        for (StateNode e1: a1.afterState) {
            for (StateNode p2: a2.beforeState) {
                if (e1.name.substring(1).equals(p2.name) || p2.name.substring(1).equals(e1.name)) {
                    actionMutex.get(layer).add(a1.name + a2.name);
                    if (!interferenceMutex.contains(a2.name + ", " + a1.name)) {
                        interferenceMutex.add(a1.name + ", " + a2.name);
                    }
                }
            }
        }
        return interferenceMutex;
    }

    public static HashSet<String> getNegatedLiterals(StateNode s1, StateNode s2, HashSet<String> negatedLiteralsMutex, int layer) {
        stateMutex.get(layer + 1).add(s1.name + s2.name);
        if (!negatedLiteralsMutex.contains(s2.name + ", " + s1.name)) {
            negatedLiteralsMutex.add(s1.name + ", " + s2.name);
        }
        return negatedLiteralsMutex;
    }

    public static HashSet<String> getInconsistentSupport(StateNode s1, StateNode s2, HashSet<String> inconsistentSupportMutex, int layer) {
        boolean inconsistentSupportExists = true;
        for (ActionNode p1: s1.beforeState) {
            for (ActionNode p2: s2.beforeState) {
                if (!actionMutex.get(layer).contains(p1.name + p2.name) && !actionMutex.get(layer).contains(p2.name + p1.name)) {
                    inconsistentSupportExists = false;
                }
            }
        }
        if (inconsistentSupportExists) {
            stateMutex.get(layer + 1).add(s1.name + s2.name);
            if (!inconsistentSupportMutex.contains(s2.name + ", " + s1.name)) {
                inconsistentSupportMutex.add(s1.name + ", " + s2.name);
            }
        }
        return inconsistentSupportMutex;
    }

    public static boolean isTimeToExtract(int layer) {
        boolean extractFlag = true;
        for (String s: goalState) {
            if (!stateNodesMap.get(layer + 1).containsKey(s)) {
                extractFlag = false; //Will lose something in new states
            } else {
                for (String t: goalState) {
                    if (stateMutex.get(layer + 1).contains(s + t)) {
                        extractFlag = false; //Same goals are mutex
                    }
                }
            }
            if (!extractFlag) {
                break;
            }
        }
        return extractFlag;
    }

    public static void extract(int currentLevel, HashSet<String> currentGoal, HashSet<String> nextGoal) {
        if (currentLevel == 0 && !solutionFlag) {
            solutionFlag = true;
        } else {
            if (currentGoal.size() == 0) {
                extract(currentLevel - 1, nextGoal, new HashSet<String>());
            } else {
                for (StateNode s: stateNodesMap.get(currentLevel).values()) {
                    if (currentGoal.contains(s.name)) {
                        for (ActionNode p: s.beforeState) {
                            boolean mutex = false;
                            for (String a: extraction.get(currentLevel - 1)) {
                                if (actionMutex.get(currentLevel - 1).contains(a + p.name)) {
                                    mutex = true;
                                }
                            }
                            if (!mutex) {
                                currentGoal.remove(s.name);
                                for (StateNode pState: p.beforeState) {
                                    nextGoal.add(pState.name);
                                }
                                extraction.get(currentLevel - 1).add(p.name);
                                extract(currentLevel, currentGoal, nextGoal);
                                extraction.get(currentLevel - 1).remove(p.name);
                                currentGoal.add(s.name);
                                for (StateNode pState: p.beforeState) {
                                    nextGoal.remove(pState.name);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        //Create file reader and writer objects
        BufferedReader fileReader = null;
        BufferedWriter fileWriter = null;
        File inputFile = null; //Input file initialization
        File outputFile = null; //Path for output file
        if (0 < args.length) {
            inputFile = new File(args[0]);
            outputFile = new File(args[1]);
        }
        processFile(fileReader, inputFile);
        //Expand graph and write output to file
        expand(fileWriter, outputFile);
    }
}
