package ru.mirea;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Optimizer {

    private final HashSet<String> opAll = new HashSet<>(Arrays.asList("~", "=", "+", "-", "*", "/", "==", "!=",
            ">=", "<=", ">", "<", "and", "or", "not", "!", "!F", "print", "add", "get", "size", "remove", "|"));
    private final HashSet<String> boolOp = new HashSet<>(Arrays.asList("==", "!=", ">=", "<=", ">", "<", "and", "or",
            "not", "!", "!F", "|"));

    /*
    все операции:
    "~", "=", "+", "-", "*", "/", "==", "!=", ">=", "<=", ">", "<", "and", "or", "not", "!", "!F", "print", "add", "get", "size", "remove", "|"
    не логические операции и не конец области видимости:
    "~", "=", "+", "-", "*", "/", "print", "add", "get", "size", "remove"
    из них что-то возвращают в стек:
    "~", "+", "-", "*", "/", "get", "size"
    из них одноместные:
    "~", "size"
    не логические операции и не конец области видимости и не возвращают ничего в стек:
    "=", "print", "add", "remove"
    из них нольместные (остальные двухместные):
    "print"
    */

    private LinkedList<String> poliz;
    private HashMap<String, Integer> transitTable;
    private LinkedList<Triad> triads = new LinkedList<>();
    private LinkedList<Variable> varTable = new LinkedList<>();
    private LinkedList<NameHashSet> nameHashSetTable = new LinkedList<>();
    private LinkedList<NameList> nameListTable = new LinkedList<>();
    private int pointer;    //текущая позиция
    private int start, end; //начало и конец оптимизируемой области
    private int scopeNumber;//номер следующей области видимости

    public Optimizer(LinkedList<String> poliz, HashMap<String, Integer> transitTable) {
        this.poliz = poliz;
        this.transitTable = transitTable;
        pointer = -1;
        scopeNumber = 1;
        start = -1;
        end = -1;
    }

    public void optimize() {
        while (setStartAndEnd()) {
            getTriads();
            System.out.println("До оптимизации");
            for (Triad t : triads) {
                t.printTriad();
            }
            run();
            System.out.println("После оптимизации");
            for (Triad t : triads) {
                t.printTriad();
            }
            getNewPoliz();
            System.out.println(poliz);
        }
    }

    private void run() {
        for (int i = 0; i < triads.size(); i++) {
            calcTriad(i);
        }
    }


    private float calcTriad(int i) {
        float arg1, arg2;
        switch (triads.get(i).getOp()) {
            case "=":
                float num = -1;
                if (!triads.get(i).getArg2().contains("^")) {
                    num = getNum(triads.get(i).getArg2());
                } else if (calcTriad((int) getNum(triads.get(i).getArg2().substring(1))) != -1) {
                    num = calcTriad((int) getNum(triads.get(i).getArg2().substring(1)));
                }
                for (Variable var : varTable) {
                    if (triads.get(i).getArg1().equals(var.name)) {
                        var.value = num;
                    }
                }
                return 0;
            case "~":
                if (!(triads.get(i).getArg1().contains("L") || triads.get(i).getArg1().contains("HS"))) {
                    varTable.add(new Variable(triads.get(i).getArg1(), 0));
                    return 0;
                }
            case "+":
                if (triads.get(i).getArg1().contains("^")) {
                    if (calcTriad((int) getNum(triads.get(i).getArg1().substring(1))) != -1) {
                        arg1 = calcTriad((int) getNum(triads.get(i).getArg1().substring(1)));
                    } else return -1;
                } else {
                    arg1 = getNum(triads.get(i).getArg1());
                }
                if (triads.get(i).getArg2().contains("^")) {
                    if (calcTriad((int) getNum(triads.get(i).getArg2().substring(1))) != -1) {
                        arg2 = calcTriad((int) getNum(triads.get(i).getArg2().substring(1)));
                    } else return -1;
                } else {
                    arg2 = getNum(triads.get(i).getArg2());
                }
                triads.set(i, new Triad((arg1 + arg2) + ""));
                return arg1 + arg2;
            case "-":
                if (triads.get(i).getArg1().contains("^")) {
                    if (calcTriad((int) getNum(triads.get(i).getArg1().substring(1))) != -1) {
                        arg1 = calcTriad((int) getNum(triads.get(i).getArg1().substring(1)));
                    } else return -1;
                } else {
                    arg1 = getNum(triads.get(i).getArg1());
                }
                if (triads.get(i).getArg2().contains("^")) {
                    if (calcTriad((int) getNum(triads.get(i).getArg2().substring(1))) != -1) {
                        arg2 = calcTriad((int) getNum(triads.get(i).getArg2().substring(1)));
                    } else return -1;
                } else {
                    arg2 = getNum(triads.get(i).getArg2());
                }
                triads.set(i, new Triad((arg1 - arg2) + ""));
                return arg1 - arg2;
            case "*":
                if (triads.get(i).getArg1().contains("^")) {
                    if (calcTriad((int) getNum(triads.get(i).getArg1().substring(1))) != -1) {
                        arg1 = calcTriad((int) getNum(triads.get(i).getArg1().substring(1)));
                    } else return -1;
                } else {
                    arg1 = getNum(triads.get(i).getArg1());
                }
                if (triads.get(i).getArg2().contains("^")) {
                    if (calcTriad((int) getNum(triads.get(i).getArg2().substring(1))) != -1) {
                        arg2 = calcTriad((int) getNum(triads.get(i).getArg2().substring(1)));
                    } else return -1;
                } else {
                    arg2 = getNum(triads.get(i).getArg2());
                }
                triads.set(i, new Triad((arg1 * arg2) + ""));
                return arg1 * arg2;
            case "/":
                if (triads.get(i).getArg1().contains("^")) {
                    if (calcTriad((int) getNum(triads.get(i).getArg1().substring(1))) != -1) {
                        arg1 = calcTriad((int) getNum(triads.get(i).getArg1().substring(1)));
                    } else return -1;
                } else {
                    arg1 = getNum(triads.get(i).getArg1());
                }
                if (triads.get(i).getArg2().contains("^")) {
                    if (calcTriad((int) getNum(triads.get(i).getArg2().substring(1))) != -1) {
                        arg2 = calcTriad((int) getNum(triads.get(i).getArg2().substring(1)));
                    } else return -1;
                } else {
                    arg2 = getNum(triads.get(i).getArg2());
                }
                triads.set(i, new Triad((arg1 / arg2) + ""));
                return arg1 / arg2;
            case "С":
                return Float.parseFloat(triads.get(i).getArg1());
            default:
                return -1;
        }
    }

    private void getTriads() {
        LinkedList<String> stack = new LinkedList<>();
        pointer = start;

        while (pointer <= end) {

            if (!opAll.contains(poliz.get(pointer))) { //если не операция
                stack.addFirst(poliz.get(pointer));

            } else if (!boolOp.contains(poliz.get(pointer))) { //если операция, но не логическая и не |
                /*
                не логические операции и не конец области видимости:
                "~", "=", "+", "-", "*", "/", "print", "add", "get", "size", "remove"
                */
                if (poliz.get(pointer).equals("=") || poliz.get(pointer).equals("add") || poliz.get(pointer).equals("remove")) {
                    /*
                    операции, которые принимают два аргумента, но ничего не возвращают:
                    "=", "add", "remove"
                    */
                    String arg2 = stack.removeFirst();
                    String arg1 = stack.removeFirst();
                    triads.add(new Triad(poliz.get(pointer), arg1, arg2));

                } else if (poliz.get(pointer).equals("~") && (stack.getFirst().endsWith("L") || stack.getFirst().endsWith("HS"))) {
                    /*
                    операция, которая принимаут один аргумент, но возвращает только в случае с переменными:
                    "~"
                    */
                    triads.add(new Triad(poliz.get(pointer), stack.removeFirst()));

                } else if (poliz.get(pointer).equals("~") || poliz.get(pointer).equals("size")) {
                    /*
                    операции, которые принимают один аргумент и возвращают результат в стек:
                    "~", "size"
                    */
                    String tmp = stack.removeFirst();
                    triads.add(new Triad(poliz.get(pointer), tmp));
                    stack.addFirst(tmp);

                } else if (poliz.get(pointer).equals("print")) {
                    /*
                    операция, которая не принимает аргумент и ничего не возвращает в стек:
                    "print"
                    */
                    triads.add(new Triad(poliz.get(pointer), "_", "_"));

                } else {
                    /*Операции, которые берут два аргумента из стека и возвращают результат в стек:
                    "+", "-", "*", "/", "get"*/
                    String arg2 = stack.removeFirst();
                    String arg1 = stack.removeFirst();
                    stack.addFirst("^" + triads.size());
                    triads.add(new Triad(poliz.get(pointer), arg1, arg2));
                }
            } else {
                /*
                логические операции и конец области видимости:
                "==", "!=", ">=", "<=", ">", "<", "and", "or", "not", "!", "!F", "|"
                */
                if (poliz.get(pointer).equals("|")) {

                }
            }
            pointer++;
        }
    }

    private boolean setStartAndEnd() {

        if (start == end) {
            if (transitTable.isEmpty()) {
                start = 0;
                end = poliz.size() - 1;
                return true;
            } else {
                start = 0;
                end = transitTable.get("p" + scopeNumber + 1) - 1;
                return true;
            }
        } else if (!transitTable.isEmpty()) {
            if (end + 1 == transitTable.get("p" + scopeNumber + 1)) {
                //тут надо проверить не используются ли уже известные переменные внутри новой области видимости
                //return true;
            }
        } else if (end + 1 == poliz.size()) {
            return false;
        }
        return false;
    }

    private void getNewPoliz() {
        int i = 0;
        while (!triads.isEmpty()) {
            Triad triad = triads.removeFirst();
            if (!triad.getOp().equals("C")) {
                if (!triad.getArg1().equals("_")) {
                    if (!triad.getArg1().contains("^")) {
                        poliz.set(start + i, triad.getArg1());
                        i++;
                    }
                    if (!triad.getArg2().equals("_") && !triad.getArg2().contains("^")) {
                        poliz.set(start + i, triad.getArg2());
                        i++;
                    }
                }
                poliz.set(start + i, triad.getOp());
                i++;
            }
        }
        while (start + i <= end) {
            poliz.remove(end);
            end--;
        }
    }

    public LinkedList<String> getPoliz() {
        System.out.println(poliz);
        return poliz;
    }

    private final HashSet<String> VARIABLE = new HashSet<>(Arrays.asList("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "a", "s", "d", "f", "g", "h", "j", "k", "l", "z", "x", "c", "v", "b", "n", "m"));

    private float getNum(String str) {
        if (VARIABLE.contains(str.substring(0, 1))) {
            for (Variable var : varTable) {
                if (var.name.equals(str)) {
                    return var.value;
                }
            }
        } else {
            return Float.parseFloat(str);
        }
        return -1;
    }
}
