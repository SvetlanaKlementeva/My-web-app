package org.mycompany.myname;

public class Calculator {
    private double a = 0;
    private double b = 0;
    private double result = 0;
    private boolean resultExists = false;
    public Calculator() {}
    public double getA() { return a; }
    public void setA(double a) { this.a = a; }
    public double getB() { return b; }
    public void setB(double b) { this.b = b; }
    public double getResult() {resultExists = false; return result; }
    public void doSum() { result = a + b; resultExists = true; }
    public void doSubtr() { result = a - b; resultExists = true; }
    public void doMult() { result = a * b; resultExists = true; }
    public void doDev() { result = a / b; resultExists = true; }
    public boolean isResultExists() { return resultExists; }
}
