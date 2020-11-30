package q4;

public class Action {
    public String actionName;
    public String[] preconditions;
    public String[] effects;

    public Action(String actionName, String[] preconditions, String[] effects) {
        this.actionName = actionName;
        this.preconditions = preconditions;
        this.effects = effects;
    }
}
