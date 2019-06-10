package ru.mirea;

import java.util.LinkedList;
import java.util.List;

public class Lexer {

    private List<String> lines;
    private List<Token> tokens = new LinkedList<>();

    public Lexer(List<String> lines) {
        this.lines = lines;
    }

    public List<Token> getTokens() {
        LexemPatterns lp = new LexemPatterns();
        String buffer = "";
        int count = 0;
        for (String line : lines) {
            count++;
            line = line.replace("\uFEFF", "");
            line = line.replace(" ", "");
            while (line.length() != 0) {
                if (!lp.getTerminal(line).equals("ERROR")) {
                    this.tokens.add(new Token(lp.getTerminal(line), line));
                    line = line.replace(line, buffer);
                    buffer = buffer.replace(buffer, "");
                } else {
                    if (line.length() == 1) {
                        System.out.println("Lexer:\nОбнаружена ошибка в коде программы! Недопустимый символ!");
                        tokens.clear();
                        tokens.add(new Token("ERROR", String.valueOf(count)));
                        return tokens;
                    }
                    buffer = line.charAt(line.length() - 1) + buffer;
                    line = line.replace(line, line.substring(0, line.length() - 1));
                }

            }
        }
        return tokens;
    }
}
