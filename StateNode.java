package q4;

import java.util.ArrayList;

public class StateNode {
    public String name;
    public ArrayList<ActionNode> beforeState;
    public ArrayList<ActionNode> afterState;

    public StateNode(String name) {
        this.name = name;
        beforeState = new ArrayList<ActionNode>();
        afterState = new ArrayList<ActionNode>();
    }
}
