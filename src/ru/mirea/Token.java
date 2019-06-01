package ru.mirea;

public class Token {
    private String typeLexeme;
    private String lexeme;

    public Token(String typeLexeme, String lexeme) {
        this.typeLexeme = typeLexeme;
        this.lexeme = lexeme;
    }

    public String getTypeLexeme() {
        return typeLexeme;
    }

    public String getLexeme() {
        return lexeme;
    }
}
