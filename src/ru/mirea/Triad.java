package ru.mirea;

public class Triad {

    private String op;
    private String arg1;
    private String arg2;

    public Triad(String op, String arg1, String arg2) {
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public Triad(String op, String arg1) {  //для одноместных операций
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = "_";
    }

    public Triad(String arg1) {
        this.op = "C";
        this.arg1 = arg1;
        this.arg2 = "0";
    }

    public void printTriad() {
        System.out.println(this.op + "(" + this.arg1 + ", " + this.arg2 + ")");
    }

    public String getOp() {
        return op;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }
}
