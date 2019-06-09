package ru.mirea;

import java.util.LinkedList;
import java.util.List;

public class Parser {

    private List<Token> tokens;
    private LinkedList<Variable> varTable = new LinkedList<>();
    private LinkedList<Variable> transitTable = new LinkedList<>();
    private LinkedList<String> poliz = new LinkedList<>();
    private LinkedList<Token> stack = new LinkedList<>();

    private int pointer;
    private int flagErr = 0;

    private int level;
    private int scope;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public LinkedList<Variable> getTransitTable() {
        return transitTable;
    }

    public LinkedList<String> getPoliz() {
        return poliz;
    }

    public void lang() {
        pointer = 0;
        level = 0;
        scope = 0;
        while (tokens.size() > pointer && flagErr == 0) {
            expr();
            while (stack.size() > 0) {
                poliz.add(stack.removeLast().getLexeme());
            }
        }
        if (flagErr > 0) {
            System.out.println("Обнаружена ошибка в синтаксисе кода!");
        } else {
            if (flagErr == 0) {
                System.out.println("Parser: Ok");
                poliz.addLast(".");
            }
        }
        varTable.clear();
    }

    public void expr() {
        switch (tokens.get(pointer).getTypeLexeme()) {
            case "INT_KW": {
                INT_KW();
                VAR();
                ASSIGN_OP();
                stmt();
                break;
            }
            case "VAR": {
                VAR();
                ASSIGN_OP();
                stmt();
                break;
            }
            case "DO_KW": {
                do_while_expr();
                break;
            }
            case "WHILE_KW": {
                while_expr();
                break;
            }
            case "FOR_KW": {
                for_expr();
                break;
            }
            case "IF_KW": {
                if_expr();
                break;
            }
            case "PRINT_KW": {
                PRINT();
                break;
            }
            case "ERROR": {
                System.out.println("Обнаружена ошибка в строке " + tokens.get(pointer).getLexeme());
                flagErr = -1;
                break;
            }
        }
    }

    private void while_expr() {
        WHILE_KW();
        cond_expr();
        body();
    }

    private void do_while_expr() {
        DO_KW();
        body();
        WHILE_KW();
        cond_expr();
    }

    private void for_expr() {
        FOR_KW();
        L_RB();
        INT_KW();
        VAR();
        ASSIGN_OP();
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("VAR")) {
                VAR();
            } else NUMBER();
        }
        COLON();
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("VAR")) {
                VAR();
            } else NUMBER();
        }
        R_RB();
        body();
    }

    private void if_expr() {
        IF_KW();
        cond_expr();
        body();
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("ELSE_KW")) {
                ELSE_KW();
                body();
            }
        }
    }

    private void cond_expr() {
        L_RB();
        bool_expr();
        R_RB();
    }

    private void bool_expr() {
        comp_expr();
        if (pointer < tokens.size() && flagErr == 0) {
            while (tokens.get(pointer).getTypeLexeme().equals("BOOL_OP")) {
                BOOL_OP();
                comp_expr();
            }
        }
    }

    private void comp_expr() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("NOT_KW")) {
                NOT_KW();
                L_RB();
                comp_expr();
                R_RB();
            } else {
                stmt();
                COMP_OP();
                stmt();
            }
        }
    }

    private void b_stmt() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("L_RB")) {
                L_RB();
                stmt();
                R_RB();
            } else {
                stmt();
            }
        }
    }

    private void stmt() {
        value();
        if (pointer < tokens.size() && flagErr == 0) {
            while (tokens.get(pointer).getTypeLexeme().equals("OP")) {
                OP();
                value();
            }
        }
    }

    private void value() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("VAR")) {
                VAR();
            } else {
                if (tokens.get(pointer).getTypeLexeme().equals("NUMBER")) {
                    NUMBER();
                } else {
                    b_stmt();
                }
            }
        }
    }

    private void body() {
        L_B();
        if (pointer < tokens.size() && flagErr == 0) {
            while (tokens.get(pointer).getTypeLexeme().equals("INT_KW") || tokens.get(pointer).getTypeLexeme().equals("VAR")
                    || tokens.get(pointer).getTypeLexeme().equals("DO_KW") || tokens.get(pointer).getTypeLexeme().equals("WHILE_KW")
                    || tokens.get(pointer).getTypeLexeme().equals("FOR_KW") || tokens.get(pointer).getTypeLexeme().equals("IF_KW")
                    || tokens.get(pointer).getTypeLexeme().equals("PRINT_KW")) {
                expr();
                if (pointer >= tokens.size()) {
                    break;
                }
            }
        } else flagErr++;
        R_B();
    }

    private void VAR() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("VAR")) {
                if (tokens.get(pointer - 1).getTypeLexeme().equals("INT_KW")) {
                    boolean flag = false;
                    for (Variable var : varTable) {
                        if (var.name.equals(tokens.get(pointer).getLexeme() + level)) {
                            flag = true;
                        }
                    }
                    if (!flag) {
                        varTable.add(new Variable(tokens.get(pointer).getLexeme() + level, 0));
                        poliz.add(tokens.get(pointer).getLexeme() + level);
                    } else flagErr++;
                } else {
                    boolean flag = false;
                    int lvl = 0;
                    for (Variable var : varTable) {
                        int i = 0;
                        do {
                            if (var.name.equals(tokens.get(pointer).getLexeme() + (level - i))) {
                                flag = true;
                                lvl = level - i;
                            }
                            i++;
                        } while (level - i >= 0);
                    }
                    if (flag) {
                        poliz.add(tokens.get(pointer).getLexeme() + lvl);
                    } else {
                        flagErr++;
                    }
                }
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void ASSIGN_OP() {  //оператор присвоения
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("ASSIGN_OP")) {
                if (stack.getLast().getTypeLexeme().equals("INT_KW")) {
                    poliz.add(stack.removeLast().getLexeme());
                }
                stack.add(tokens.get(pointer));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void INT_KW() {     //объявление переменной
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("INT_KW")) {
                if (stack.size() != 0) {
                    while (stack.getLast().getTypeLexeme().equals("OP") || stack.getLast().getTypeLexeme().equals("ASSIGN_OP")) {
                        poliz.add(stack.removeLast().getLexeme());
                        if (stack.size() == 0) {
                            break;
                        }
                    }
                }
                stack.add(new Token(tokens.get(pointer).getTypeLexeme(), "~"));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void WHILE_KW() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("WHILE_KW")) {
                if (stack.size() != 0) {
                    while (stack.getLast().getTypeLexeme().equals("OP") || stack.getLast().getTypeLexeme().equals("ASSIGN_OP")) {
                        poliz.add(stack.removeLast().getLexeme());
                        if (stack.size() == 0) {
                            break;
                        }
                    }
                }
                if (stack.size() != 0) {
                    if (!stack.getLast().getTypeLexeme().equals("DO")) {
                        level++;
                        scope++;
                        transitTable.add(new Variable("p" + scope + 1, poliz.size()));
                        transitTable.add(new Variable("p" + scope + 2, -1));
                        stack.addLast(new Token("WHILE1", "p" + scope + 2));
                        stack.addLast(new Token("WHILE0", "!"));
                        stack.addLast(new Token("WHILE0", "p" + scope + 1));
                        stack.addLast(new Token("WHILE", "!F"));
                        stack.addLast(new Token("WHILE", "p" + scope + 2));
                    }
                } else {
                    level++;
                    scope++;
                    transitTable.add(new Variable("p" + scope + 1, poliz.size()));
                    transitTable.add(new Variable("p" + scope + 2, -1));
                    stack.addLast(new Token("WHILE1", "p" + scope + 2));
                    stack.addLast(new Token("WHILE0", "!"));
                    stack.addLast(new Token("WHILE0", "p" + scope + 1));
                    stack.addLast(new Token("WHILE", "!F"));
                    stack.addLast(new Token("WHILE", "p" + scope + 2));
                }
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void DO_KW() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("DO_KW")) {
                level++;
                if (stack.size() != 0) {
                    while (stack.getLast().getTypeLexeme().equals("OP") || stack.getLast().getTypeLexeme().equals("ASSIGN_OP")) {
                        poliz.add(stack.removeLast().getLexeme());
                        if (stack.size() == 0) {
                            break;
                        }
                    }
                }
                scope++;
                transitTable.add(new Variable("p" + scope + 1, poliz.size()));
                transitTable.add(new Variable("p" + scope + 2, -1));
                stack.addLast(new Token("DO1", "p" + scope + 2));
                stack.addLast(new Token("DO", "!"));
                stack.addLast(new Token("DO", "p" + scope + 1));
                stack.addLast(new Token("DO", "!F"));
                stack.addLast(new Token("DO", "p" + scope + 2));

            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void IF_KW() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("IF_KW")) {
                level++;
                scope++;
                transitTable.add(new Variable("p" + scope + 1, -1));
                if (stack.size() != 0) {
                    while (stack.getLast().getTypeLexeme().equals("OP") || stack.getLast().getTypeLexeme().equals("ASSIGN_OP")) {
                        poliz.add(stack.removeLast().getLexeme());
                        if (stack.size() == 0) {
                            break;
                        }
                    }
                }
                stack.addLast(new Token("IF1", "p" + scope + 1));
                stack.addLast(new Token("IF", "!F"));
                stack.addLast(new Token("IF", "p" + scope + 1));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void ELSE_KW() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("ELSE_KW")) {
                level++;
                scope++;
                transitTable.add(new Variable("p" + scope + 2, -1));
                if (stack.size() != 0) {
                    while (stack.getLast().getTypeLexeme().equals("OP") || stack.getLast().getTypeLexeme().equals("ASSIGN_OP")) {
                        poliz.add(stack.removeLast().getLexeme());
                        if (stack.size() == 0) {
                            break;
                        }
                    }
                }
                stack.addLast(new Token("ELSE1", "p" + scope + 2));
                stack.addLast(new Token("ELSE", "!"));
                stack.addLast(new Token("ELSE", "p" + scope + 2));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void FOR_KW() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("FOR_KW")) {
                level++;
                scope++;
                transitTable.add(new Variable("p" + scope + 1, -1));
                transitTable.add(new Variable("p" + scope + 2, -1));
                transitTable.add(new Variable("p" + scope + 3, -1));
                transitTable.add(new Variable("p" + scope + 4, -1));
                if (stack.size() != 0) {
                    while (stack.getLast().getTypeLexeme().equals("OP") || stack.getLast().getTypeLexeme().equals("ASSIGN_OP")) {
                        poliz.add(stack.removeLast().getLexeme());
                        if (stack.size() == 0) {
                            break;
                        }
                    }
                }
                stack.addLast(new Token("FOR1", "!"));
                stack.addLast(new Token("FOR1", "p" + scope + 2));
                stack.addLast(new Token("FOR", "!"));
                stack.addLast(new Token("FOR", "p" + scope + 1));
                stack.addLast(new Token("FOR", "="));
                stack.addLast(new Token("FOR", "+"));
                stack.addLast(new Token("FOR", "1"));
                if (pointer + 3 < tokens.size()) {
                    stack.addLast(new Token("FOR", tokens.get(pointer + 3).getLexeme() + level));
                    stack.addLast(new Token("FOR0", tokens.get(pointer + 3).getLexeme() + level));
                } else flagErr++;
                stack.addLast(new Token("FOR", "!"));
                stack.addLast(new Token("FOR", "p" + scope + 3));
                stack.addLast(new Token("FOR", "!F"));
                stack.addLast(new Token("FOR", "p" + scope + 4));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void NOT_KW() {     //логичеткий оператор инверсии
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("NOT_KW")) {
                stack.add(tokens.get(pointer));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void BOOL_OP() {    //логические операторы (конъюнкция, дизъюнкция, исключающее "или")
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("BOOL_OP")) {
                if (stack.size() != 0) {
                    while (!(stack.getLast().getTypeLexeme().equals("L_RB") || stack.getLast().getTypeLexeme().equals("BOOL_OP"))) {
                        poliz.add(stack.removeLast().getLexeme());
                        if (stack.size() == 0) {
                            break;
                        }
                    }
                    if (stack.size() != 0) {
                        if (tokens.get(pointer).getLexeme().equals("or")) {
                            while (stack.getLast().getTypeLexeme().equals("BOOL_OP")) {
                                poliz.add(stack.removeLast().getLexeme());
                                if (stack.size() == 0) {
                                    break;
                                }
                            }
                        } else {
                            while (stack.getLast().getLexeme().equals("and")) {
                                poliz.add(stack.removeLast().getLexeme());
                                if (stack.size() == 0) {
                                    break;
                                }
                            }
                        }
                    }
                }
                stack.add(tokens.get(pointer));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void COMP_OP() {    //операторы сравнения
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("COMP_OP")) {
                if (stack.size() != 0) {
                    while (stack.getLast().getTypeLexeme().equals("OP")) {
                        poliz.add(stack.removeLast().getLexeme());
                        if (stack.size() == 0) {
                            break;
                        }
                    }
                }
                stack.add(tokens.get(pointer));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void OP() {         //арифметические операторы
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("OP")) {
                if (stack.size() != 0) {
                    if (tokens.get(pointer).getLexeme().equals("+") || tokens.get(pointer).getLexeme().equals("-")) {
                        while (stack.getLast().getLexeme().equals("*") || stack.getLast().getLexeme().equals("/")) {
                            poliz.add(stack.removeLast().getLexeme());
                            if (stack.size() == 0) {
                                break;
                            }
                        }
                        if (stack.size() != 0) {
                            while (stack.getLast().getLexeme().equals("-") || stack.getLast().getLexeme().equals("+")) {
                                poliz.add(stack.removeLast().getLexeme());
                                if (stack.size() == 0) {
                                    break;
                                }
                            }
                        }
                    } else {
                        while (stack.getLast().getLexeme().equals("*") || stack.getLast().getLexeme().equals("/")) {
                            poliz.add(stack.removeLast().getLexeme());
                            if (stack.size() == 0) {
                                break;
                            }
                        }
                    }
                }
                stack.add(tokens.get(pointer));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void L_RB() {   //открывающая круглая скобка
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("L_RB")) {
                stack.addLast(tokens.get(pointer));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void R_RB() {   //закрывающая круглая скобка
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("R_RB")) {
                if (stack.size() != 0) {
                    while (!stack.getLast().getTypeLexeme().equals("L_RB")) {
                        poliz.add(stack.removeLast().getLexeme());
                        if (stack.size() == 0) {
                            flagErr++;
                            break;
                        }
                    }
                    stack.removeLast();
                    if (stack.size() != 0) {
                        if (stack.getLast().getTypeLexeme().equals("DO")) {
                            while (stack.getLast().getTypeLexeme().equals("DO")) {
                                poliz.add(stack.removeLast().getLexeme());
                                if (stack.size() == 0) {
                                    break;
                                }
                            }
                            if (stack.size() != 0) {
                                int i = 0;
                                while (true) {
                                    if (transitTable.get(transitTable.size() - 1 - i).name.equals(stack.getLast().getLexeme())) {
                                        break;
                                    }
                                    i++;
                                }
                                transitTable.set(transitTable.size() - 1 - i, new Variable(stack.getLast().getLexeme(), poliz.size()));
                                stack.removeLast();
                            }
                        }
                    }
                }
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void L_B() {    //открывающая фигурная скобка
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("L_B")) {
                if (stack.getLast().getTypeLexeme().equals("FOR")) {
                    while (!stack.getLast().getTypeLexeme().equals("FOR1")) {
                        if (stack.getLast().getTypeLexeme().equals("FOR0")) {
                            int i = 0;
                            int j = 0;
                            while (true) {
                                if (transitTable.size() - 1 - i >= 0) {
                                    if (transitTable.get(transitTable.size() - 1 - i).name.equals("p" + (scope - j) + 2) && transitTable.get(transitTable.size() - 1 - i).value == -1) {
                                        break;
                                    }
                                    i++;
                                } else {
                                    j++;
                                    i = 0;
                                }
                            }
                            transitTable.set(transitTable.size() - 1 - i, new Variable("p" + (scope - j) + 2, poliz.size()));
                        }
                        poliz.add(stack.removeLast().getLexeme());
                        if (stack.size() == 0) {
                            flagErr++;
                            break;
                        }
                    }
                    int i = 0;
                    int j = 0;
                    while (true) {
                        if (transitTable.size() - 1 - i >= 0) {
                            if (transitTable.get(transitTable.size() - 1 - i).name.equals("p" + (scope - j) + 3) && transitTable.get(transitTable.size() - 1 - i).value == -1) {
                                break;
                            }
                            i++;
                        } else {
                            j++;
                            i = 0;
                        }
                    }
                    transitTable.set(transitTable.size() - 1 - i, new Variable("p" + (scope - j) + 3, poliz.size()));
                } else {
                    if (stack.getLast().getTypeLexeme().equals("IF")) {
                        while (stack.getLast().getTypeLexeme().equals("IF")) {
                            poliz.add(stack.removeLast().getLexeme());
                            if (stack.size() == 0) {
                                flagErr++;
                                break;
                            }
                        }
                    } else {
                        if (stack.getLast().getTypeLexeme().equals("ELSE")) {
                            while (stack.getLast().getTypeLexeme().equals("ELSE")) {
                                poliz.add(stack.removeLast().getLexeme());
                                if (stack.size() == 0) {
                                    flagErr++;
                                    break;
                                }
                            }
                        } else {
                            if (stack.getLast().getTypeLexeme().equals("WHILE")) {
                                while (stack.getLast().getTypeLexeme().equals("WHILE")) {
                                    poliz.add(stack.removeLast().getLexeme());
                                    if (stack.size() == 0) {
                                        flagErr++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                stack.addLast(tokens.get(pointer));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void R_B() {    //закрывающая фигурная скобка
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("R_B")) {
                if (stack.size() != 0) {
                    while (!stack.getLast().getTypeLexeme().equals("L_B")) {
                        poliz.add(stack.removeLast().getLexeme());
                        if (stack.size() == 0) {
                            flagErr++;
                            break;
                        }
                    }
                    stack.removeLast();

                    if (stack.size() != 0) {
                        if (stack.getLast().getTypeLexeme().equals("FOR1")) {
                            while (stack.getLast().getTypeLexeme().equals("FOR1")) {
                                poliz.add(stack.removeLast().getLexeme());
                                if (stack.size() == 0) {
                                    flagErr++;
                                    break;
                                }
                            }
                            int i = 0;
                            int j = 0;
                            while (true) {
                                if (transitTable.size() - 1 - i >= 0) {
                                    if (transitTable.get(transitTable.size() - 1 - i).name.equals("p" + (scope - j) + 4) && transitTable.get(transitTable.size() - 1 - i).value == -1) {
                                        break;
                                    }
                                    i++;
                                } else {
                                    j++;
                                    i = 0;
                                }
                            }
                            transitTable.set(transitTable.size() - 1 - i, new Variable("p" + (scope - j) + 4, poliz.size()));
                        } else {
                            if (stack.getLast().getTypeLexeme().equals("IF1")) {
                                int i = 0;
                                while (true) {
                                    if (transitTable.get(transitTable.size() - 1 - i).name.equals(stack.getLast().getLexeme())) {
                                        break;
                                    }
                                    i++;
                                }
                                transitTable.set(transitTable.size() - 1 - i, new Variable(stack.getLast().getLexeme(), poliz.size()));
                                stack.removeLast();
                            } else {
                                if (stack.getLast().getTypeLexeme().equals("ELSE1")) {
                                    int i = 0;
                                    while (true) {
                                        if (transitTable.get(transitTable.size() - 1 - i).name.equals(stack.getLast().getLexeme())) {
                                            break;
                                        }
                                        i++;
                                    }
                                    transitTable.set(transitTable.size() - 1 - i, new Variable(stack.getLast().getLexeme(), poliz.size()));
                                    stack.removeLast();
                                } else {
                                    if (stack.getLast().getTypeLexeme().equals("WHILE0")) {
                                        while (stack.getLast().getTypeLexeme().equals("WHILE0")) {
                                            poliz.add(stack.removeLast().getLexeme());
                                            if (stack.size() == 0) {
                                                flagErr++;
                                                break;
                                            }
                                        }
                                        int i = 0;
                                        while (true) {
                                            if (transitTable.get(transitTable.size() - 1 - i).name.equals(stack.getLast().getLexeme())) {
                                                break;
                                            }
                                            i++;
                                        }
                                        transitTable.set(transitTable.size() - 1 - i, new Variable(stack.getLast().getLexeme(), poliz.size()));
                                        stack.removeLast();
                                    }
                                }
                            }
                        }
                    }
                    do {
                        if (varTable.getLast().name.contains("" + level)) {
                            varTable.removeLast();
                        } else break;
                    } while (true);
                    level--;
                }
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void COLON() {  //двоеточие, используется только в цикле for
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("COLON")) {
                poliz.add(stack.removeLast().getLexeme());
                int i = 0;
                int j = 0;
                while (true) {
                    if (transitTable.size() - 1 - i >= 0) {
                        if (transitTable.get(transitTable.size() - 1 - i).name.equals("p" + (scope - j) + 1) && transitTable.get(transitTable.size() - 1 - i).value == -1) {
                            break;
                        }
                        i++;
                    } else {
                        j++;
                        i = 0;
                    }
                }
                transitTable.set(transitTable.size() - 1 - i, new Variable("p" + (scope - j) + 1, poliz.size()));
                poliz.add(tokens.get(pointer - 3).getLexeme() + level);
                stack.addLast(new Token("COMP_OP", "<"));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void NUMBER() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("NUMBER")) {
                poliz.add(tokens.get(pointer).getLexeme());
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void PRINT() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("PRINT")) {
                if (stack.size() != 0) {
                    while (stack.getLast().getTypeLexeme().equals("OP") || stack.getLast().getTypeLexeme().equals("ASSIGN_OP")) {
                        poliz.add(stack.removeLast().getLexeme());
                        if (stack.size() == 0) {
                            break;
                        }
                    }
                }
                poliz.add(tokens.get(pointer).getLexeme());
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }
}
