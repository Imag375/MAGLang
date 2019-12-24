package ru.mirea;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("code.txt"), StandardCharsets.UTF_8);
            Lexer lexer = new Lexer(lines);
            Parser parser = new Parser(lexer.getTokens());
            if (parser.lang() == 0) {
                Optimizer optimizer = new Optimizer(parser.getPoliz(), parser.getTransitTable());
                optimizer.optimize();
                StackMachine stackMachine = new StackMachine(optimizer.getPoliz(), parser.getTransitTable());
                stackMachine.SM();
            }
        } catch (IOException e) {
            System.out.println("Не могу прочесть файл :(");
        }

    }

}
