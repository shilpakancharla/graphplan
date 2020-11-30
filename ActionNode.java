package q4;

import java.util.ArrayList;

public class ActionNode {
    public String name;
    public ArrayList<StateNode> beforeState;
    public ArrayList<StateNode> afterState;

    public ActionNode(String name) {
        this.name = name;
        beforeState = new ArrayList<StateNode>();
        afterState = new ArrayList<StateNode>();
    }
}
