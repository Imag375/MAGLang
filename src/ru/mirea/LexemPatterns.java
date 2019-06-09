package ru.mirea;

public class LexemPatterns {

    private final String NUMBERS = "0123456789";
    private final String VARIABLE = "qwertyuiopasdfghjklzxcvbnm";

    public String getTerminal(String lexeme) {
        if (lexeme.contains("int") || lexeme.contains("while") || lexeme.contains("do") || lexeme.contains("if")
                || lexeme.contains("else") || lexeme.contains("for") || lexeme.contains("not") || lexeme.contains("and")
                || lexeme.contains("or") || lexeme.contains("xor")) {
            if (lexeme.equals("int")) {
                return "INT_KW";
            }
            if (lexeme.equals("while")) {
                return "WHILE_KW";
            }
            if (lexeme.equals("do")) {
                return "DO_KW";
            }
            if (lexeme.equals("if")) {
                return "IF_KW";
            }
            if (lexeme.equals("else")) {
                return "ELSE_KW";
            }
            if (lexeme.equals("for")) {
                return "FOR_KW";
            }
            if (lexeme.equals("not")) {
                return "NOT_KW";
            }
            if (lexeme.equals("and") || lexeme.equals("or")) {
                return "BOOL_OP";
            }
        } else {
            if (isVariable(lexeme)) {
                return "VAR";
            }
        }
        if (lexeme.equals("==") || lexeme.equals("!=") || lexeme.equals(">=") || lexeme.equals("<=") || lexeme.equals(">") || lexeme.equals("<")) {
            return "COMP_OP";
        }
        if (lexeme.equals("+") || lexeme.equals("-") || lexeme.equals("*") || lexeme.equals("/")) {
            return "OP";
        }
        if (lexeme.equals("(")) {
            return "L_RB";
        }
        if (lexeme.equals(")")) {
            return "R_RB";
        }
        if (lexeme.equals("{")) {
            return "L_B";
        }
        if (lexeme.equals("}")) {
            return "R_B";
        }
        if (lexeme.equals(":")) {
            return "COLON";
        }
        if (lexeme.equals("=")) {
            return "ASSIGN_OP";
        }
        if (isNumber(lexeme)) {
            return "NUMBER";
        }

        return "ERROR";
    }

    private boolean isNumber(String lexeme) {
        for (char ch : lexeme.toCharArray()) {
            if (!NUMBERS.contains(String.valueOf(ch))) {
                return false;
            }
        }
        return true;
    }

    private boolean isVariable(String lexeme) {
        for (char ch : lexeme.toCharArray()) {
            if (!VARIABLE.contains(String.valueOf(ch))) {
                return false;
            }
        }
        return true;
    }
}
