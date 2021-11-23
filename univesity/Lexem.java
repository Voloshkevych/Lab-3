package univesity;

public class Lexem {
    private String letters;
    private LexemType type;

    Lexem(){
        this.letters = "";
    }

    Lexem(LexemType t, String s){
        this.letters = s;
        this.type = t;
    }

    void append(String s){
        letters = letters.concat(s);
    }

    public String toString(){
        return String.format("<%s> - <%s>", type, letters);
    }

    void setType(LexemType t){
        type = t;
    }

    LexemType getType(){
        return type;
    }

    String getLetters(){
        return letters;
    }
}
