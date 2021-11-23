package univesity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Automaton {
    private final State start;
    private State current;
    private State incorrect = new State("incorrect", true, LexemType.NOT_RECOGNIZED);
    private final List<String> keyWords = new ArrayList<>(Arrays.asList(
            "abstract", "arguments", "await", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "debugger", "default", "delete", "do",
            "double", "else", "enum", "eval", "export", "extends", "false", "final", "finally", "float", "for", "function", "goto", "if", "implements", "import",
            "in", "instanceof", "int", "interface", "let", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typeof", "var", "void", "volatile", "while", "with", "yield" ));

    private List<String> digits10 = new ArrayList<>(Arrays.asList("0123456789".split("")));
    private List<String> digits8 = new ArrayList<>( Arrays.asList("01234567".split("")));
    private List<String> digits16 = new ArrayList<>(Arrays.asList("0123456789abcdefABCDEF".split("")));
    private List<String> letters = new ArrayList<>(Arrays.asList("qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM".split("")));
    private List<String> ops = new ArrayList<>(Arrays.asList("+-/*><=!|&%".split("")));
    private List<String> punct = new ArrayList<>(Arrays.asList(",;:()[]{}.".split("")));
    private List<String> whites = new ArrayList<>(Arrays.asList(" ", "\t", "\n", "\r"));
    private List<String> any = new ArrayList<>();
    private List<String> anyWithSpace = new ArrayList<>();
    private List<String> anyWithSpaceAndNewline = new ArrayList<>();


    Automaton() {
        any.addAll(letters);
        any.addAll(digits10);
        //any.addAll(ops);
        //any.addAll(punct);
        List<String> others = new ArrayList<>(Arrays.asList("_~@#$^&?'`\"\\".split("")));
        any.addAll(others);
        anyWithSpace.addAll(any);
        anyWithSpace.addAll(ops);
        anyWithSpace.addAll(punct);
        anyWithSpace.add(" ");
        anyWithSpace.add("\t");
        anyWithSpaceAndNewline.addAll(anyWithSpace);
        anyWithSpaceAndNewline.add("\r");
        anyWithSpaceAndNewline.add("\n");
        start = new State("Start parsing...", false, LexemType.NOT_RECOGNIZED);
        incorrect.appendRule(any, incorrect);
    }


    List<Lexem> recognize(String s){
        ArrayList<Lexem> res = new ArrayList<>();
        int i = 0;
        while (i<s.length()){
            if (s.charAt(i) == ' ') i++;
            Lexem lexem = this.parseWord(s, i);
            if (keyWords.contains(lexem.getLetters())){
                lexem.setType(LexemType.KEYWORD);
            }
            if (lexem.getLetters().length() == 0){
                try {
                    res.add(new Lexem(LexemType.NOT_RECOGNIZED, String.valueOf(s.charAt(i))));
                    i++;
                }
                catch (StringIndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
            else {
                res.add(lexem);
//            System.out.println(lexem.getType());
                i += lexem.getLetters().length();
            }
        }
        return res;
    }

    private Lexem parseWord(String s, Integer from){
        this.current = start;
        Lexem res = new Lexem();
        int i = from;
        while(i < s.length()){
            LexemType type = this.putSymbol(String.valueOf(s.charAt(i)));
            if (type != null) {
                res.setType(type);
                res.append(String.valueOf(s.charAt(i)));
            }
            else return res;
            i++;
        }
        return res;
    }

    private LexemType putSymbol(String s){
        try {
//            System.out.printf("char %s ", s);
            State next = this.current.map(s);
            if (next == null) return null;
            else current = next;
//            System.out.println(this.current.getName());
            return current.getType();
        }
        catch (NullPointerException e){
            return LexemType.NOT_RECOGNIZED;
        }
    }

    void initAsCppLexer(){
        initIdentifierStates();
        initNumericStates();
        initStringCharStates();
        initCommentStates();
        initWhiteStates();
        initPunctuationOperatorStates();
    }

    private void initIdentifierStates(){
        State q1 = new State("got identifier", true, LexemType.IDENTIFIER);
        start.appendRule(letters, q1);
        start.appendRule("_", q1);
        start.appendRule("$", q1);
        q1.appendRule(letters, q1);
        q1.appendRule(digits10, q1);
        q1.appendRule("_", q1);
        q1.appendRule("$", q1);
    }

    private void initNumericStates(){
        State q0 = new State("zero after start", true, LexemType.INT10);
        start.appendRule("0", q0);
        initIntegerStates(q0);
        initOctoStates(q0);
        initHexStates(q0);
    }

    private void initIntegerStates(State s){

        State q1 = new State("got number 1-9 from start, it`s float or int10", true, LexemType.INT10);
        List<String> oneToNine = new ArrayList<>(Arrays.asList("123456789".split("")));
        start.appendRule(oneToNine, q1);
        q1.appendRule(any, incorrect);
        q1.appendRule(digits10, q1);

        State q2 = new State("got . after int", true, LexemType.FLOAT);
        q2.appendRule(any, incorrect);
        q2.appendRule(digits10, q2);
        q1.appendRule(".",q2);

        s.appendRule(".", q2);

        State q3 = new State("got e", false, LexemType.NOT_RECOGNIZED);
        State q4 = new State("got numbers after e", true, LexemType.FLOAT);
        q3.appendRule(any, incorrect);
        q3.appendRule(punct, incorrect);
        q3.appendRule(ops, incorrect);

        q3.appendRule("-", q3);
        q3.appendRule(digits10, q4);
        q1.appendRule("e", q3);
        q2.appendRule("e", q3);
        q4.appendRule(any, incorrect);
        q4.appendRule(digits10, q4);

    }

    private void initOctoStates(State s){
        State q1 = new State("got 0, then 1-7", true, LexemType.INT8);
        //List<String> oneToSeven = new ArrayList<>(Arrays.asList("1234567".split("")));
        s.appendRule(digits8, q1);
        q1.appendRule(any, incorrect);
        q1.appendRule(digits8 ,q1);

        State q2 = new State("got oct but then smth > 7", true, LexemType.INT10);
        s.appendRule("8", q2);
        s.appendRule("9", q2);
        q1.appendRule("8", q2);
        q1.appendRule("9", q2);
        q2.appendRule(any, incorrect);
        q2.appendRule(digits10, q2);

        State q3 = new State("got o after 0", false, LexemType.NOT_RECOGNIZED);
        s.appendRule("o", q3);
        q3.appendRule(any, incorrect);
        q3.appendRule(digits8, q1);
    }

    private void initHexStates(State s){
        State q0 = new State("got x after 0", false, LexemType.NOT_RECOGNIZED);
        s.appendRule(Arrays.asList("xX".split("")), q0);
        State q1 = new State("got 1-f", true, LexemType.INT16);
        //List<String> oneToSixteen = new ArrayList<>(Arrays.asList("123456789abcdef".split("")));
        q0.appendRule(any, incorrect);
        q0.appendRule(ops, incorrect);
        q0.appendRule(punct, incorrect);
        q0.appendRule(digits16, q1);
        q1.appendRule(any, incorrect);
        q1.appendRule(digits16 ,q1);
    }


    private void initStringCharStates(){
        State q0  = new State("String started", false, LexemType.NOT_RECOGNIZED);
        start.appendRule("\"", q0);
        q0.appendRule(anyWithSpaceAndNewline, q0);
        State q1 = new State("String finished", false, LexemType.STRING);
        q0.appendRule("\"", q1);
        q1.appendRule(any, incorrect);

//        State q2  = new State("Char started", false, LexemType.NOT_RECOGNIZED);
//        start.appendRule("'", q2);
//        State q3 = new State("Got char", false, LexemType.NOT_RECOGNIZED);
//        q2.appendRule(anyWithSpaceAndNewline, q3);
//        State q4 = new State("Char finished", true, LexemType.CHARACTER);
//
//        q4.appendRule(any, incorrect);
//
//        State q5 = new State("Empty string with ''", true, LexemType.STRING);
//        q2.appendRule("'", q5);
//        q5.appendRule(any, incorrect);
//        State q6 = new State("Actually a string", false, LexemType.NOT_RECOGNIZED);
//        q3.appendRule(anyWithSpaceAndNewline, q6);
//
//        q3.appendRule("'", q4);
//
//        q6.appendRule(anyWithSpaceAndNewline, q6);
//        State q7 = new State("String finished", false, LexemType.STRING);
//        q6.appendRule("'", q7);
//        q7.appendRule(any, incorrect);

        q0  = new State("String started with `", false, LexemType.NOT_RECOGNIZED);
        start.appendRule("`", q0);
        q0.appendRule(anyWithSpaceAndNewline, q0);
        q1 = new State("String finished", false, LexemType.STRING);
        q0.appendRule("`", q1);
        q1.appendRule(any, incorrect);

        q0  = new State("String started with '", false, LexemType.NOT_RECOGNIZED);
        start.appendRule("'", q0);
        q0.appendRule(anyWithSpaceAndNewline, q0);
        q1 = new State("String finished", false, LexemType.STRING);
        q0.appendRule("'", q1);
        q1.appendRule(any, incorrect);



    }

    private void initCommentStates(){
        State q0 = new State("/ input", false, LexemType.OPERATOR);
        start.appendRule("/", q0);

        q0.appendRule("=", new State("/=", true, LexemType.OPERATOR));

        State q1 = new State("line comment input", true, LexemType.COMMENT);
        q0.appendRule("/", q1);
        q1.appendRule(anyWithSpace, q1);
        State q2 = new State("multiline comment start", false, LexemType.NOT_RECOGNIZED);
        q0.appendRule("*", q2);
        q2.appendRule(anyWithSpaceAndNewline, q2);
        State q3 = new State("got * after multiline comment", false, LexemType.NOT_RECOGNIZED);
        q2.appendRule("*", q3);
        q3.appendRule(anyWithSpaceAndNewline, q2);
        State q4 = new State("finished multiline comment", true, LexemType.COMMENT);
        q3.appendRule("/", q4);
        //State q5 = new State("newline or tab input", true, LexemType.COMMENT);
    }

    private void initWhiteStates(){
        State q0 = new State("whitespace", true, LexemType.WHITE);
        start.appendRule(whites, q0);

    }

    private void initPunctuationOperatorStates(){
        State q0 = new State("Punctuation", true, LexemType.PUNCTUATION);
        start.appendRule(punct, q0);

        q0 = new State("+ input", true, LexemType.OPERATOR);
        start.appendRule("+", q0);
        State q1 = new State("++ or +=", true, LexemType.OPERATOR);
        q0.appendRule(Arrays.asList("+=".split("")), q1);

        q0 = new State("- input", true, LexemType.OPERATOR);
        start.appendRule("-", q0);
        q1 = new State("- or -=", true, LexemType.OPERATOR);
        q0.appendRule(Arrays.asList("-=".split("")), q1);

        q0 = new State("! input", true, LexemType.OPERATOR);
        start.appendRule("!", q0);
        q1 = new State("!=", true, LexemType.OPERATOR);
        q0.appendRule("=", q1);

        q0 = new State("* input", true, LexemType.OPERATOR);
        start.appendRule("*", q0);
        q1 = new State("*=", true, LexemType.OPERATOR);
        q0.appendRule("=", q1);


        q0 = new State("& input", true, LexemType.OPERATOR);
        start.appendRule("&", q0);
        q1 = new State("&&", true, LexemType.OPERATOR);
        q0.appendRule("&", q1);

        q0 = new State("| input", true, LexemType.OPERATOR);
        start.appendRule("|", q0);
        q1 = new State("||", true, LexemType.OPERATOR);
        q0.appendRule("|", q1);

        q0 = new State("> input", true, LexemType.OPERATOR);
        start.appendRule(">", q0);
        q1 = new State(">>", true, LexemType.OPERATOR);
        q0.appendRule(">", q1);
        State q2 = new State(">=", true, LexemType.OPERATOR);
        q0.appendRule("=", q2);


        q0 = new State("< input", true, LexemType.OPERATOR);
        start.appendRule("<", q0);
        q1 = new State("<<", true, LexemType.OPERATOR);
        q0.appendRule("<", q1);
        q2 = new State("<=", true, LexemType.OPERATOR);
        q0.appendRule("=", q2);


        q0 = new State("= input", true, LexemType.OPERATOR);
        start.appendRule("=", q0);
        q1 = new State("==", true, LexemType.OPERATOR);
        q0.appendRule("=", q1);
        q2 = new State("===", true, LexemType.OPERATOR);
        q1.appendRule("=", q2);
    }
}
