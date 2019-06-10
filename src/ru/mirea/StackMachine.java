package ru.mirea;

import java.util.LinkedList;

public class StackMachine {

    private final String VARIABLE = "qwertyuiopasdfghjklzxcvbnm";

    private LinkedList<Variable> varTable = new LinkedList<>();
    private LinkedList<NameHashSet> nameHashSetTable = new LinkedList<>();
    private LinkedList<NameList> nameListTable = new LinkedList<>();
    private LinkedList<Variable> transitTable;
    private LinkedList<String> poliz;
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
                    if (!(stack.getFirst().contains("L") || stack.getFirst().contains("HS"))) {
                        varTable.add(new Variable(stack.getFirst(), 0));
                    } else {
                        if (stack.getFirst().contains("L")) {
                            nameListTable.add(new NameList(stack.getFirst()));
                        } else {
                            nameHashSetTable.add(new NameHashSet(stack.getFirst()));
                        }
                        stack.removeFirst();
                    }
                    pointer++;
                    break;
                }
                case "=": {
                    float num = getNum();
                    stack.removeFirst();
                    for (Variable var : varTable) {
                        if (var.name.equals(stack.getFirst())) {
                            var.value = num;
                        }
                    }
                    stack.removeFirst();
                    pointer++;
                    break;
                }
                case "+": {
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    stack.addFirst((num1 + num2) + "");
                    pointer++;
                    break;
                }
                case "-": {
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    stack.addFirst((num1 - num2) + "");
                    pointer++;
                    break;
                }
                case "*": {
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    stack.addFirst((num1 * num2) + "");
                    pointer++;
                    break;
                }
                case "/": {
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    stack.addFirst((num1 / num2) + "");
                    pointer++;
                    break;
                }
                case "==": {
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    if (num1 == num2) {
                        stack.addFirst("true");
                    } else {
                        stack.addFirst("false");
                    }
                    pointer++;
                    break;
                }
                case "!=": {
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    if (num1 != num2) {
                        stack.addFirst("true");
                    } else {
                        stack.addFirst("false");
                    }
                    pointer++;
                    break;
                }
                case ">=": {
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    if (num1 >= num2) {
                        stack.addFirst("true");
                    } else {
                        stack.addFirst("false");
                    }
                    pointer++;
                    break;
                }
                case "<=": {
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    if (num1 <= num2) {
                        stack.addFirst("true");
                    } else {
                        stack.addFirst("false");
                    }
                    pointer++;
                    break;
                }
                case ">": {
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    if (num1 > num2) {
                        stack.addFirst("true");
                    } else {
                        stack.addFirst("false");
                    }
                    pointer++;
                    break;
                }
                case "<": {
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    if (num1 < num2) {
                        stack.addFirst("true");
                    } else {
                        stack.addFirst("false");
                    }
                    pointer++;
                    break;
                }
                case "and": {
                    boolean a, b;
                    if (stack.removeFirst().equals("true")) {
                        b = true;
                    } else {
                        b = false;
                    }
                    if (stack.removeFirst().equals("true")) {
                        a = true;
                    } else {
                        a = false;
                    }
                    if (a && b) {
                        stack.addFirst("true");
                    } else {
                        stack.addFirst("false");
                    }
                    pointer++;
                    break;
                }
                case "or": {
                    boolean a, b;
                    if (stack.removeFirst().equals("true")) {
                        b = true;
                    } else {
                        b = false;
                    }
                    if (stack.removeFirst().equals("true")) {
                        a = true;
                    } else {
                        a = false;
                    }
                    if (a || b) {
                        stack.addFirst("true");
                    } else {
                        stack.addFirst("false");
                    }
                    pointer++;
                    break;
                }
                case "not": {
                    boolean a, b;
                    if (stack.removeFirst().equals("true")) {
                        a = true;
                    } else {
                        a = false;
                    }
                    if (!a) {
                        stack.addFirst("true");
                    } else {
                        stack.addFirst("false");
                    }
                    pointer++;
                    break;
                }
                case "!": {
                    int num = pointer;
                    for (Variable var : transitTable) {
                        if (var.name.equals(stack.getFirst())) {
                            num = (int) var.value;
                        }
                    }
                    stack.removeFirst();
                    pointer = num;
                    break;
                }
                case "!F": {
                    int num = pointer;
                    for (Variable var : transitTable) {
                        if (var.name.equals(stack.getFirst())) {
                            num = (int) var.value;
                            break;
                        }
                    }
                    stack.removeFirst();
                    boolean a;
                    if (stack.removeFirst().equals("true")) {
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
                    System.out.println("\nТаблица переменных:");
                    if (varTable.size() > 0) {
                        for (Variable str : varTable) {
                            System.out.println(str.name + " : " + str.value);
                        }
                    }
                    System.out.println("Таблица LinkedList:");
                    if (nameListTable.size() > 0) {
                        for (NameList str : nameListTable) {
                            System.out.println(str.name + " : " + str.list);
                        }
                    }
                    System.out.println("Таблица HashSet:");
                    if (nameHashSetTable.size() > 0) {
                        for (NameHashSet str : nameHashSetTable) {
                            System.out.println(str.name + " : " + str.set);
                        }
                    }
                    pointer++;
                    break;
                }
                case "add": {
                    int num = (int) getNum();
                    stack.removeFirst();
                    if (stack.getFirst().contains("L")) {
                        for (NameList var : nameListTable) {
                            if (var.name.equals(stack.getFirst())) {
                                var.list.add(num);
                                break;
                            }
                        }
                    } else {
                        if (stack.getFirst().contains("HS")) {
                            for (NameHashSet var : nameHashSetTable) {
                                if (var.name.equals(stack.getFirst())) {
                                    var.set.add(num);
                                    break;
                                }
                            }
                        }
                    }
                    stack.removeFirst();
                    pointer++;
                    break;
                }
                case "get": {
                    int num = (int) getNum();
                    stack.removeFirst();
                    if (stack.getFirst().contains("L")) {
                        for (NameList var : nameListTable) {
                            if (var.name.equals(stack.getFirst())) {
                                if (num < var.list.size()) {
                                    stack.removeFirst();
                                    stack.addFirst(var.list.get(num) + "");
                                }
                                break;
                            }
                        }
                    }
                    pointer++;
                    break;
                }
                case "size": {
                    if (stack.getFirst().contains("L")) {
                        for (NameList var : nameListTable) {
                            if (var.name.equals(stack.getFirst())) {
                                stack.removeFirst();
                                stack.addFirst(var.list.size() + "");
                                break;
                            }
                        }
                    } else {
                        if (stack.getFirst().contains("HS")) {
                            for (NameHashSet var : nameHashSetTable) {
                                if (var.name.equals(stack.getFirst())) {
                                    stack.removeFirst();
                                    stack.addFirst(var.set.size() + "");
                                    break;
                                }
                            }
                        }
                    }
                    pointer++;
                    break;
                }
                case "remove": {
                    int num = (int) getNum();
                    stack.removeFirst();
                    if (stack.getFirst().contains("L")) {
                        for (NameList var : nameListTable) {
                            if (var.name.equals(stack.getFirst())) {
                                if (var.list.contains(num)) {
                                    var.list.remove(var.list.lastIndexOf(num));
                                }
                                break;
                            }
                        }
                    }
                    if (stack.getFirst().contains("HS")) {
                        for (NameHashSet var : nameHashSetTable) {
                            if (var.name.equals(stack.getFirst())) {
                                if (var.set.contains(num)) {
                                    var.set.remove(num);
                                }
                                break;
                            }
                        }
                    }
                    stack.removeFirst();
                    pointer++;
                    break;
                }
                case "|": { //конец области видимости, удаляем переменные, которые ей принадлежат
                    int level = (int) getNum();
                    stack.removeFirst();
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
                    pointer++;
                    break;
                }
                default: {
                    stack.addFirst(poliz.get(pointer));
                    pointer++;
                }
            }
        }
    }

    private float getNum() {
        if (VARIABLE.contains(stack.getFirst().substring(0, 1))) {
            for (Variable var : varTable) {
                if (var.name.equals(stack.getFirst())) {
                    return var.value;
                }
            }
        } else {
            return Float.parseFloat(stack.getFirst());
        }
        return -1;
    }
}
