package ru.mirea;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Parser {

    private List<Token> tokens;
    private LinkedList<Variable> varTable = new LinkedList<>();
    private LinkedList<NameHashSet> nameHashSetTable = new LinkedList<>();
    private LinkedList<NameList> nameListTable = new LinkedList<>();
    private HashMap<String, Integer> transitTable = new HashMap<>();
    private LinkedList<String> poliz = new LinkedList<>();
    private LinkedList<Token> stack = new LinkedList<>();

    private int pointer;
    private int flagErr = 0;

    private int level;
    private int scope;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public HashMap<String, Integer> getTransitTable() {
        return transitTable;
    }

    public LinkedList<String> getPoliz() {
        return poliz;
    }

    public int lang() {
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
        } else if (flagErr == 0) {
            System.out.println("Lexer: Ok");
            System.out.println("Parser: Ok");
            System.out.println("\nОбратная польская запись:");
            for (String str : poliz) {
                System.out.print(str + " ");
            }
            System.out.println("\nТаблица значений переходов:");
            for (Map.Entry entry : transitTable.entrySet()) {
                System.out.println(entry);
            }
        }
        varTable.clear();
        return flagErr;
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
                switch (tokens.get(pointer).getTypeLexeme()) {
                    case "ASSIGN_OP": {
                        ASSIGN_OP();
                        stmt();
                        break;
                    }
                    case "POINT": {
                        POINT();
                        FUNCTION();
                        L_RB();
                        stmt();
                        R_RB();
                        break;
                    }
                }
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
                PRINT_KW();
                break;
            }
            case "LIST_KW": {
                LIST_KW();
                POINT();
                CREATE_KW();
                L_RB();
                VAR();
                R_RB();
                break;
            }
            case "HASH_SET_KW": {
                HASH_SET_KW();
                POINT();
                CREATE_KW();
                L_RB();
                VAR();
                R_RB();
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
                if (tokens.get(pointer).getTypeLexeme().equals("POINT")) {
                    POINT();
                    if (tokens.get(pointer).getTypeLexeme().equals("GET_KW")) {
                        GET_KW();
                        L_RB();
                        stmt();
                        R_RB();
                    } else if (tokens.get(pointer).getTypeLexeme().equals("SIZE_KW")) {
                        SIZE_KW();
                    }
                }
            } else if (tokens.get(pointer).getTypeLexeme().equals("NUMBER")) {
                NUMBER();
            } else {
                b_stmt();
            }
        }
    }

    private void body() {
        L_B();
        if (pointer < tokens.size() && flagErr == 0) {
            while (tokens.get(pointer).getTypeLexeme().equals("INT_KW") || tokens.get(pointer).getTypeLexeme().equals("VAR")
                    || tokens.get(pointer).getTypeLexeme().equals("DO_KW") || tokens.get(pointer).getTypeLexeme().equals("WHILE_KW")
                    || tokens.get(pointer).getTypeLexeme().equals("FOR_KW") || tokens.get(pointer).getTypeLexeme().equals("IF_KW")
                    || tokens.get(pointer).getTypeLexeme().equals("PRINT_KW") || tokens.get(pointer).getTypeLexeme().equals("LIST_KW")
                    || tokens.get(pointer).getTypeLexeme().equals("HASH_SET_KW")) {
                expr();
                if (pointer >= tokens.size()) {
                    break;
                }
            }
        } else flagErr++;
        R_B();
    }

    private void VAR() {    //какая-то переменная, возможно, что имя списка или хеш-сета
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("VAR")) {
                if (tokens.get(pointer - 1).getTypeLexeme().equals("INT_KW")) {
                    boolean flag = false;
                    int i = 0;
                    do {
                        for (Variable var : varTable) {
                            if (var.name.equals(tokens.get(pointer).getLexeme() + (level - i))) {
                                flag = true;
                                break;
                            }
                        }
                        i++;
                    } while (level - i >= 0);
                    if (!flag) {
                        varTable.add(new Variable(tokens.get(pointer).getLexeme() + level, 0));
                        poliz.add(tokens.get(pointer).getLexeme() + level);
                    } else flagErr++;
                } else {    //если не создается новая переменная, то ищем уже существующую
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
                        if (tokens.get(pointer - 1).getTypeLexeme().equals("VAR") || tokens.get(pointer - 1).getTypeLexeme().equals("NUMBER")) {
                            if (stack.size() != 0) {
                                crowdingOut();
                            }
                        }
                        poliz.add(tokens.get(pointer).getLexeme() + lvl);
                    } else if (pointer + 1 < tokens.size() && (nameListTable.size() > 0 || nameHashSetTable.size() > 0)) {
                        if (tokens.get(pointer + 1).getTypeLexeme().equals("POINT")) {
                            flag = false;
                            lvl = 0;
                            if (nameListTable.size() > 0) {
                                for (NameList var : nameListTable) {
                                    int i = 0;
                                    do {
                                        if (var.name.equals(tokens.get(pointer).getLexeme() + (level - i) + "L")) {
                                            flag = true;
                                            lvl = level - i;
                                            break;
                                        }
                                        i++;
                                    } while (level - i >= 0);
                                }
                            }
                            if (flag) {
                                if (stack.size() != 0 && tokens.get(pointer + 2).getTypeLexeme().equals("FUNCTION")) {
                                    crowdingOut();
                                }
                                poliz.add(tokens.get(pointer).getLexeme() + lvl + "L");
                            } else {
                                if (nameHashSetTable.size() > 0) {
                                    for (NameHashSet var : nameHashSetTable) {
                                        int i = 0;
                                        do {
                                            if (var.name.equals(tokens.get(pointer).getLexeme() + (level - i) + "HS")) {
                                                flag = true;
                                                lvl = level - i;
                                            }
                                            i++;
                                        } while (level - i >= 0);
                                    }
                                }
                                if (flag) {
                                    if (stack.size() != 0 && tokens.get(pointer + 2).getTypeLexeme().equals("FUNCTION")) {
                                        crowdingOut();
                                    }
                                    poliz.add(tokens.get(pointer).getLexeme() + lvl + "HS");
                                } else flagErr++;
                            }
                        } else if (tokens.get(pointer - 2).getTypeLexeme().equals("CREATE_KW")) {
                            flag = false;
                            int i = 0;
                            do {
                                for (Variable var : varTable) {
                                    if (var.name.equals(tokens.get(pointer).getLexeme() + (level - i))) {
                                        flag = true;
                                        break;
                                    }
                                }
                                for (NameList var : nameListTable) {
                                    if (var.name.equals(tokens.get(pointer).getLexeme() + level + "L")) {
                                        flag = true;
                                        break;
                                    }
                                }
                                for (NameHashSet var : nameHashSetTable) {
                                    if (var.name.equals(tokens.get(pointer).getLexeme() + level + "HS")) {
                                        flag = true;
                                        break;
                                    }
                                }
                                i++;
                            } while (level - i >= 0);
                            if (!flag) {
                                if (tokens.get(pointer - 4).getTypeLexeme().equals("LIST_KW")) {
                                    nameListTable.add(new NameList(tokens.get(pointer).getLexeme() + level + "L"));
                                    poliz.add(tokens.get(pointer).getLexeme() + level + "L");
                                } else {
                                    nameHashSetTable.add(new NameHashSet(tokens.get(pointer).getLexeme() + level + "HS"));
                                    poliz.add(tokens.get(pointer).getLexeme() + level + "HS");
                                }
                            } else flagErr++;
                        } else flagErr++;
                    } else if (tokens.get(pointer - 2).getTypeLexeme().equals("CREATE_KW")) {
                        flag = false;
                        int i = 0;
                        do {
                            for (Variable var : varTable) {
                                if (var.name.equals(tokens.get(pointer).getLexeme() + (level - i))) {
                                    flag = true;
                                    break;
                                }
                            }
                            for (NameList var : nameListTable) {
                                if (var.name.equals(tokens.get(pointer).getLexeme() + level + "L")) {
                                    flag = true;
                                    break;
                                }
                            }
                            for (NameHashSet var : nameHashSetTable) {
                                if (var.name.equals(tokens.get(pointer).getLexeme() + level + "HS")) {
                                    flag = true;
                                    break;
                                }
                            }
                            i++;
                        } while (level - i >= 0);
                        if (!flag) {
                            if (tokens.get(pointer - 4).getTypeLexeme().equals("LIST_KW")) {
                                nameListTable.add(new NameList(tokens.get(pointer).getLexeme() + level + "L"));
                                poliz.add(tokens.get(pointer).getLexeme() + level + "L");
                            } else {
                                nameHashSetTable.add(new NameHashSet(tokens.get(pointer).getLexeme() + level + "HS"));
                                poliz.add(tokens.get(pointer).getLexeme() + level + "HS");
                            }
                        } else flagErr++;
                    } else flagErr++;
                }
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void ASSIGN_OP() {  //оператор присваивания
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("ASSIGN_OP")) {
                if (stack.size() != 0) {
                    if (stack.getLast().getTypeLexeme().equals("INT_KW")) {
                        poliz.add(stack.removeLast().getLexeme());
                    }
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
                    crowdingOut();
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
                    crowdingOut();
                }
                if (stack.size() != 0) {
                    if (!stack.getLast().getTypeLexeme().equals("DO")) {
                        level++;
                        scope++;
                        transitTable.put("p" + scope + 1, poliz.size());
                        transitTable.put("p" + scope + 2, -1);
                        stack.addLast(new Token("WHILE1", "p" + scope + 2));
                        stack.addLast(new Token("WHILE0", "!"));
                        stack.addLast(new Token("WHILE0", "p" + scope + 1));
                        stack.addLast(new Token("WHILE", "!F"));
                        stack.addLast(new Token("WHILE", "p" + scope + 2));
                    }
                } else {
                    level++;
                    scope++;
                    transitTable.put("p" + scope + 1, poliz.size());
                    transitTable.put("p" + scope + 2, -1);
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
                    crowdingOut();
                }
                scope++;
                transitTable.put("p" + scope + 1, poliz.size());
                transitTable.put("p" + scope + 2, -1);
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
                transitTable.put("p" + scope + 1, -1);
                if (stack.size() != 0) {
                    crowdingOut();
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
                transitTable.put("p" + scope + 2, -1);
                if (stack.size() != 0) {
                    crowdingOut();
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
                transitTable.put("p" + scope + 1, -1);
                transitTable.put("p" + scope + 2, -1);
                transitTable.put("p" + scope + 3, -1);
                transitTable.put("p" + scope + 4, -1);
                if (stack.size() != 0) {
                    crowdingOut();
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
                    crowdingOut();
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
                    if (stack.getLast().getTypeLexeme().equals("GET_KW") || stack.getLast().getTypeLexeme().equals("SIZE_KW")) {
                        poliz.add(stack.removeLast().getLexeme());
                    }
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
                                transitTable.replace(stack.getLast().getLexeme(), poliz.size());
                                stack.removeLast();
                                poliz.addLast("" + level);
                                poliz.addLast("|");     //это флаг для стек машины, чтобы она знала, что область видимости закончилась
                                // и надо удалить переменные, принадлежащие этой области видимости
                                if (varTable.size() > 0) {
                                    do {
                                        if (varTable.getLast().name.contains("" + level)) {
                                            varTable.removeLast();
                                        } else break;
                                        if (varTable.size() == 0) {
                                            break;
                                        }
                                    } while (true);
                                }
                                if (nameListTable.size() > 0) {
                                    do {
                                        if (nameListTable.getLast().name.contains("" + level)) {
                                            nameListTable.removeLast();
                                        } else break;
                                        if (nameListTable.size() == 0) {
                                            break;
                                        }
                                    } while (true);
                                }
                                if (nameHashSetTable.size() > 0) {
                                    do {
                                        if (nameHashSetTable.getLast().name.contains("" + level)) {
                                            nameHashSetTable.removeLast();
                                        } else break;
                                        if (nameHashSetTable.size() == 0) {
                                            break;
                                        }
                                    } while (true);
                                }
                                level--;
                            }
                        } else if (stack.getLast().getTypeLexeme().equals("FUNCTION")) {
                            poliz.add(stack.removeLast().getLexeme());
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
                switch (stack.getLast().getTypeLexeme()) {
                    case "FOR":
                        while (!stack.getLast().getTypeLexeme().equals("FOR1")) {
                            if (stack.getLast().getTypeLexeme().equals("FOR0")) {
                                int j = 0;
                                while (true) {
                                    if (transitTable.containsKey("p" + (scope - j) + 2) && transitTable.get("p" + (scope - j) + 2) == -1) {
                                        break;
                                    } else {
                                        j++;
                                    }
                                }
                                transitTable.replace("p" + (scope - j) + 2, poliz.size());
                            }
                            poliz.add(stack.removeLast().getLexeme());
                            if (stack.size() == 0) {
                                flagErr++;
                                break;
                            }
                        }
                        int j = 0;
                        while (true) {
                            if (transitTable.containsKey("p" + (scope - j) + 3) && transitTable.get("p" + (scope - j) + 3) == -1) {
                                break;
                            } else {
                                j++;
                            }
                        }
                        transitTable.replace("p" + (scope - j) + 3, poliz.size());
                        break;
                    case "IF":
                        while (stack.getLast().getTypeLexeme().equals("IF")) {
                            poliz.add(stack.removeLast().getLexeme());
                            if (stack.size() == 0) {
                                flagErr++;
                                break;
                            }
                        }
                        break;
                    case "ELSE":
                        while (stack.getLast().getTypeLexeme().equals("ELSE")) {
                            poliz.add(stack.removeLast().getLexeme());
                            if (stack.size() == 0) {
                                flagErr++;
                                break;
                            }
                        }
                        break;
                    case "WHILE":
                        while (stack.getLast().getTypeLexeme().equals("WHILE")) {
                            poliz.add(stack.removeLast().getLexeme());
                            if (stack.size() == 0) {
                                flagErr++;
                                break;
                            }
                        }
                        break;
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
                        switch (stack.getLast().getTypeLexeme()) {
                            case "FOR1":
                                while (stack.getLast().getTypeLexeme().equals("FOR1")) {
                                    poliz.add(stack.removeLast().getLexeme());
                                    if (stack.size() == 0) {
                                        flagErr++;
                                        break;
                                    }
                                }
                                int j = 0;
                                while (true) {
                                    if (transitTable.containsKey("p" + (scope - j) + 4) && transitTable.get("p" + (scope - j) + 4) == -1) {
                                        break;
                                    } else {
                                        j++;
                                    }
                                }
                                transitTable.replace("p" + (scope - j) + 4, poliz.size());
                                break;
                            case "IF1":
                            case "ELSE1":
                                transitTable.replace(stack.removeLast().getLexeme(), poliz.size());
                                break;
                            case "WHILE0":
                                while (stack.getLast().getTypeLexeme().equals("WHILE0")) {
                                    poliz.add(stack.removeLast().getLexeme());
                                    if (stack.size() == 0) {
                                        flagErr++;
                                        break;
                                    }
                                }
                                transitTable.replace(stack.removeLast().getLexeme(), poliz.size());
                                break;
                        }
                    }
                    if (stack.size() == 0) {
                        poliz.addLast("" + level);
                        poliz.addLast("|");
                        if (varTable.size() > 0) {
                            do {
                                if (varTable.getLast().name.contains("" + level)) {
                                    varTable.removeLast();
                                } else break;
                                if (varTable.size() == 0) {
                                    break;
                                }
                            } while (true);
                        }
                        if (nameListTable.size() > 0) {
                            do {
                                if (nameListTable.getLast().name.contains("" + level)) {
                                    nameListTable.removeLast();
                                } else break;
                                if (nameListTable.size() == 0) {
                                    break;
                                }
                            } while (true);
                        }
                        if (nameHashSetTable.size() > 0) {
                            do {
                                if (nameHashSetTable.getLast().name.contains("" + level)) {
                                    nameHashSetTable.removeLast();
                                } else break;
                                if (nameHashSetTable.size() == 0) {
                                    break;
                                }
                            } while (true);
                        }
                        level--;
                    } else {
                        if (!stack.getLast().getTypeLexeme().equals("DO")) {
                            poliz.addLast("" + level);
                            poliz.addLast("|");
                            if (varTable.size() > 0) {
                                do {
                                    if (varTable.getLast().name.contains("" + level)) {
                                        varTable.removeLast();
                                    } else break;
                                    if (varTable.size() == 0) {
                                        break;
                                    }
                                } while (true);
                            }
                            if (nameListTable.size() > 0) {
                                do {
                                    if (nameListTable.getLast().name.contains("" + level)) {
                                        nameListTable.removeLast();
                                    } else break;
                                    if (nameListTable.size() == 0) {
                                        break;
                                    }
                                } while (true);
                            }
                            if (nameHashSetTable.size() > 0) {
                                do {
                                    if (nameHashSetTable.getLast().name.contains("" + level)) {
                                        nameHashSetTable.removeLast();
                                    } else break;
                                    if (nameHashSetTable.size() == 0) {
                                        break;
                                    }
                                } while (true);
                            }
                            level--;
                        }
                    }
                }
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void COLON() {  //двоеточие, используется только в цикле for
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("COLON")) {
                poliz.add(stack.removeLast().getLexeme());
                int j = 0;
                while (true) {
                    if (transitTable.containsKey("p" + (scope - j) + 1) && transitTable.get("p" + (scope - j) + 1) == -1) {
                        break;
                    } else {
                        j++;
                    }
                }
                transitTable.replace("p" + (scope - j) + 1, poliz.size());
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

    private void PRINT_KW() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("PRINT_KW")) {
                if (stack.size() != 0) {
                    crowdingOut();
                }
                poliz.add(tokens.get(pointer).getLexeme());
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void LIST_KW() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("LIST_KW")) {
                if (stack.size() != 0) {
                    crowdingOut();
                }
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void HASH_SET_KW() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("HASH_SET_KW")) {
                if (stack.size() != 0) {
                    crowdingOut();
                }
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void POINT() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (!tokens.get(pointer).getTypeLexeme().equals("POINT"))
                flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void CREATE_KW() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("CREATE_KW")) {
                if (stack.size() != 0) {
                    while (stack.getLast().getLexeme().equals("~")) {
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

    private void FUNCTION() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("FUNCTION")) {
                stack.add(tokens.get(pointer));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void SIZE_KW() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("SIZE_KW")) {
                stack.add(tokens.get(pointer));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void GET_KW() {
        if (pointer < tokens.size() && flagErr == 0) {
            if (tokens.get(pointer).getTypeLexeme().equals("GET_KW")) {
                stack.add(tokens.get(pointer));
            } else flagErr++;
        } else flagErr++;
        pointer++;
    }

    private void crowdingOut() { //просто метод, который вытесняет элементы из стека
        while (stack.getLast().getTypeLexeme().equals("OP") || stack.getLast().getTypeLexeme().equals("ASSIGN_OP")
                || stack.getLast().getTypeLexeme().equals("SIZE_KW") || stack.getLast().getTypeLexeme().equals("GET_KW")
                || stack.getLast().getLexeme().equals("~")) {
            poliz.add(stack.removeLast().getLexeme());
            if (stack.size() == 0) {
                break;
            }
        }
    }
}
