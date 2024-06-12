import java.io.*;
import java.util.*;

public class Calculator {

    private static final String HISTORY_FILE = "history.txt";
    
    // Метод для сложения
    public static double add(double a, double b) {
        return a + b;
    }

    // Метод для вычитания
    public static double subtract(double a, double b) {
        return a - b;
    }

    // Метод для умножения
    public static double multiply(double a, double b) {
        return a * b;
    }

    // Метод для деления
    public static double divide(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Деление на 0");
        }
        return a / b;
    }

    // Метод для целочисленного деления
    public static int intDivide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Деление на 0");
        }
        return a / b;
    }

    // Метод для остатка от деления
    public static int mod(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Деление на 0");
        }
        return a % b;
    }

    // Метод для возведения в степень
    public static double power(double a, double b) {
        return Math.pow(a, b);
    }

    // Метод для модуля числа
    public static double modulus(double a) {
        return Math.abs(a);
    }

    // Метод для парсинга и вычисления выражения
    public static double evaluate(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Неожиданный символ: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm(); // сложение
                    else if (eat('-')) x -= parseTerm(); // вычитание
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor(); // умножение
                    else if (eat('/')) {
                        if (eat('/')) { 
                            // Если следующий символ тоже '/', выполняется целочисленное деление
                            x = intDivide((int)x, (int)parseFactor()); 
                        } else {
                            // Иначе выполняется обычное деление
                            x = divide(x, parseFactor()); 
                        }
                    } else if (eat('%')) x = mod((int)x, (int)parseFactor()); // остаток от деления
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // унарный плюс
                if (eat('-')) return -parseFactor(); // унарный минус

                double x;
                int startPos = this.pos;
                if (eat('(')) { // скобки
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // числа
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else if (ch == '|') { // модуль
                    nextChar();
                    x = parseExpression();
                    if (!eat('|')) throw new RuntimeException("Ожидался '|'");
                    x = modulus(x);
                } else {
                    throw new RuntimeException("Неожиданный символ: " + (char)ch);
                }

                if (eat('^')) x = power(x, parseFactor()); // возведение в степень

                return x;
            }
        }.parse();
    }

    // Метод для сохранения истории вычислений
    public static void saveHistory(List<String> history) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE))) {
            for (String record : history) {
                writer.write(record);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для загрузки истории вычислений
    public static List<String> loadHistory() {
        List<String> history = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return history;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> history = loadHistory();

        System.out.println("Калькулятор запущен. Чтобы выйти, введите 'exit'.");
        while (true) {
            System.out.print("Введите выражение: ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            try {
                double result = evaluate(input);
                String record = input + " = " + result;
                history.add(record);
                System.out.println(record);
                saveHistory(history);
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
        scanner.close();
    }
}
