package ru.mirea;

import java.util.LinkedList;

public class StackMachine {

    private final String VARIABLE = "qwertyuiopasdfghjklzxcvbnm";

    private LinkedList<Variable> varTable = new LinkedList<>();
    private LinkedList<Variable> transitTable = new LinkedList<>();
    private LinkedList<String> poliz = new LinkedList<>();
    private LinkedList<String> stack = new LinkedList();

    private int pointer;

    public StackMachine(LinkedList<String> poliz, LinkedList<Variable> transitTable) {
        this.poliz = poliz;
        this.transitTable = transitTable;
        pointer = 0;
    }

    public void SM() {
        while (poliz.size() > pointer) {
            switch (poliz.get(pointer)) {
                case "~": {
                    varTable.add(new Variable(stack.getLast(), 0));
                    pointer++;
                    break;
                }
                case "=": {
                    float num = getNum();
                    stack.removeLast();
                    for (Variable var : varTable) {
                        if (var.name.equals(stack.getLast())) {
                            var.value = num;
                        }
                    }
                    stack.removeLast();
                    pointer++;
                    break;
                }
                case "+": {
                    float num2 = getNum();
                    stack.removeLast();
                    float num1 = getNum();
                    stack.removeLast();
                    stack.addLast((num1 + num2) + "");
                    pointer++;
                    break;
                }
                case "-": {
                    float num2 = getNum();
                    stack.removeLast();
                    float num1 = getNum();
                    stack.removeLast();
                    stack.addLast((num1 - num2) + "");
                    pointer++;
                    break;
                }
                case "*": {
                    float num2 = getNum();
                    stack.removeLast();
                    float num1 = getNum();
                    stack.removeLast();
                    stack.addLast((num1 * num2) + "");
                    pointer++;
                    break;
                }
                case "/": {
                    float num2 = getNum();
                    stack.removeLast();
                    float num1 = getNum();
                    stack.removeLast();
                    stack.addLast((num1 / num2) + "");
                    pointer++;
                    break;
                }
                case "==": {
                    float num2 = getNum();
                    stack.removeLast();
                    float num1 = getNum();
                    stack.removeLast();
                    if (num1 == num2) {
                        stack.addLast("true");
                    } else {
                        stack.addLast("false");
                    }
                    pointer++;
                    break;
                }
                case "!=": {
                    float num2 = getNum();
                    stack.removeLast();
                    float num1 = getNum();
                    stack.removeLast();
                    if (num1 != num2) {
                        stack.addLast("true");
                    } else {
                        stack.addLast("false");
                    }
                    pointer++;
                    break;
                }
                case ">=": {
                    float num2 = getNum();
                    stack.removeLast();
                    float num1 = getNum();
                    stack.removeLast();
                    if (num1 >= num2) {
                        stack.addLast("true");
                    } else {
                        stack.addLast("false");
                    }
                    pointer++;
                    break;
                }
                case "<=": {
                    float num2 = getNum();
                    stack.removeLast();
                    float num1 = getNum();
                    stack.removeLast();
                    if (num1 <= num2) {
                        stack.addLast("true");
                    } else {
                        stack.addLast("false");
                    }
                    pointer++;
                    break;
                }
                case ">": {
                    float num2 = getNum();
                    stack.removeLast();
                    float num1 = getNum();
                    stack.removeLast();
                    if (num1 > num2) {
                        stack.addLast("true");
                    } else {
                        stack.addLast("false");
                    }
                    pointer++;
                    break;
                }
                case "<": {
                    float num2 = getNum();
                    stack.removeLast();
                    float num1 = getNum();
                    stack.removeLast();
                    if (num1 < num2) {
                        stack.addLast("true");
                    } else {
                        stack.addLast("false");
                    }
                    pointer++;
                    break;
                }
                case "and": {
                    boolean a, b;
                    if (stack.removeLast().equals("true")) {
                        b = true;
                    } else {
                        b = false;
                    }
                    if (stack.removeLast().equals("true")) {
                        a = true;
                    } else {
                        a = false;
                    }
                    if (a && b) {
                        stack.addLast("true");
                    } else {
                        stack.addLast("false");
                    }
                    pointer++;
                    break;
                }
                case "or": {
                    boolean a, b;
                    if (stack.removeLast().equals("true")) {
                        b = true;
                    } else {
                        b = false;
                    }
                    if (stack.removeLast().equals("true")) {
                        a = true;
                    } else {
                        a = false;
                    }
                    if (a || b) {
                        stack.addLast("true");
                    } else {
                        stack.addLast("false");
                    }
                    pointer++;
                    break;
                }
                case "not": {
                    boolean a, b;
                    if (stack.removeLast().equals("true")) {
                        a = true;
                    } else {
                        a = false;
                    }
                    if (!a) {
                        stack.addLast("true");
                    } else {
                        stack.addLast("false");
                    }
                    pointer++;
                    break;
                }
                case "!": {
                    int num = pointer;
                    for (Variable var : transitTable) {
                        if (var.name.equals(stack.getLast())) {
                            num = (int) var.value;
                        }
                    }
                    stack.removeLast();
                    pointer = num;
                    break;
                }
                case "!F": {
                    int num = pointer;
                    for (Variable var : transitTable) {
                        if (var.name.equals(stack.getLast())) {
                            num = (int) var.value;
                            break;
                        }
                    }
                    stack.removeLast();
                    boolean a;
                    if (stack.removeLast().equals("true")) {
                        a = true;
                    } else {
                        a = false;
                    }
                    if (!a) {
                        pointer = num;
                    } else pointer++;
                    break;
                }
                case "print": {
                    pointer++;
                    break;
                }
                case ".": {
                    pointer++;
                    break;
                }
                default: {
                    stack.addLast(poliz.get(pointer));
                    pointer++;
                }
            }
            if (varTable.size() > 0) {
                for (Variable str : varTable) {
                    System.out.println(str.name + " : " + str.value);
                }
            }
            if (stack.size() > 0) {
                for (String str : stack) {
                    System.out.println(str);
                }
            }
        }
    }

    private float getNum() {
        if (VARIABLE.contains(stack.getLast().substring(0, 1))) {
            for (Variable var : varTable) {
                if (var.name.equals(stack.getLast())) {
                    return var.value;
                }
            }
        } else {
            return Float.parseFloat(stack.getLast());
        }
        return -1;
    }
}
