package univesity;

import java.util.HashMap;
import java.util.List;

class State {
    private String name;
    private HashMap<String, State> rules;
    private boolean isFinal;
    private LexemType type;

    State(String s, boolean isFinal, LexemType type) {
        name = s;
        this.type = type;
        this.isFinal = isFinal;
        rules = new HashMap<>();
    }

    LexemType getType() {
        return type;
    }

    String getName() {
        return name;
    }

    void appendRule(List<String> o, State s){
        for (String i:o) {
            this.appendRule(i, s);
        }
    }

    void appendRule(String o, State s) {
        rules.put(o, s);
    }

    State map(String o) {
        return rules.get(o);
    }
}
