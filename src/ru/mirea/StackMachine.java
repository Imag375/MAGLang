package ru.mirea;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class StackMachine {
    private final HashSet<String> VARIABLE = new HashSet<>(Arrays.asList("q","w","e","r","t","y","u","i","o","p","a","s","d","f","g","h","j","k","l","z","x","c","v","b","n","m"));

    private LinkedList<Variable> varTable = new LinkedList<>();
    private LinkedList<NameHashSet> nameHashSetTable = new LinkedList<>();
    private LinkedList<NameList> nameListTable = new LinkedList<>();
    private HashMap<String, Integer> transitTable;
    private LinkedList<String> poliz;
    private LinkedList<String> stack = new LinkedList();

    private int pointer;

    public StackMachine(LinkedList<String> poliz, HashMap<String, Integer> transitTable) {
        this.poliz = poliz;
        this.transitTable = transitTable;
        pointer = 0;
    }

    public void SM() {
        while (poliz.size() > pointer) {
            switch (poliz.get(pointer)) {
                case "~": { // оператор создания новой переменной
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
                case "=": { // оператор присваивания
                    float num = getNum();
                    stack.removeFirst();
                    for (Variable var : varTable) {
                        if (stack.getFirst().equals(var.name)) {
                            var.value = num;
                        }
                    }
                    stack.removeFirst();
                    pointer++;
                    break;
                }
                case "+": { // оператор арифметического сложения
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    stack.addFirst((num1 + num2) + "");
                    pointer++;
                    break;
                }
                case "-": { // оператор арифметического вычитания
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    stack.addFirst((num1 - num2) + "");
                    pointer++;
                    break;
                }
                case "*": { // оператор арифметического умножения
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    stack.addFirst((num1 * num2) + "");
                    pointer++;
                    break;
                }
                case "/": { // оператор арифметического деления
                    float num2 = getNum();
                    stack.removeFirst();
                    float num1 = getNum();
                    stack.removeFirst();
                    stack.addFirst((num1 / num2) + "");
                    pointer++;
                    break;
                }
                case "==": {    // равенство
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
                case "!=": {    //неравенство
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
                case ">=": {    //больше или равно
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
                case "<=": {    // меньше или равно
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
                case ">": { //больше
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
                case "<": { //меньше
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
                case "and": {   //конъюнкция (логическое умножение)
                    boolean a, b;
                    b = stack.removeFirst().equals("true");
                    a = stack.removeFirst().equals("true");
                    if (a && b) {
                        stack.addFirst("true");
                    } else {
                        stack.addFirst("false");
                    }
                    pointer++;
                    break;
                }
                case "or": {    //дизъюнкция (оператор логического сложения)
                    boolean a, b;
                    b = stack.removeFirst().equals("true");
                    a = stack.removeFirst().equals("true");
                    if (a || b) {
                        stack.addFirst("true");
                    } else {
                        stack.addFirst("false");
                    }
                    pointer++;
                    break;
                }
                case "not": {   // инверсия (логическое НЕ)
                    boolean a;
                    a = stack.removeFirst().equals("true");
                    if (!a) {
                        stack.addFirst("true");
                    } else {
                        stack.addFirst("false");
                    }
                    pointer++;
                    break;
                }
                case "!": { //оператор безусловного перехода
                    pointer = transitTable.get(stack.removeFirst());
                    break;
                }
                case "!F": {    //оператор условного перехода
                    int num = transitTable.get(stack.removeFirst());
                    boolean a = stack.removeFirst().equals("true");
                    if (!a) {
                        pointer = num;
                    } else pointer++;
                    break;
                }
                case "print": { //вызов функции для вывода на экран таблицы переменных
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
                case "add": {   //вызов метода для добавления нового элемента в список
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
                case "get": {   //вызов метода для получения элемента из списка
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
                case "size": {  //вызов метода для выяснения размера списка или HashSet
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
                case "remove": {    //вызов метода для удаления элемента
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
                                var.set.remove(num);
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
