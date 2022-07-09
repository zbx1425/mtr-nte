package cn.zbx1425.sowcerext.multipart.animated.script;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/// <summary>Contains functions for dealing with function script notation</summary>
public class Notation {
    /// <summary>Converts a String formatted in simple function script to postfix notation</summary>
    /// <param name="Expression">The function script String</param>
    public static String GetPostfixNotationFromFunctionNotation(String Expression) {
        int i = Expression.indexOf('[');
        if (i >= 0) {
            if (!Expression.endsWith("]")) {
                throw new IllegalArgumentException("Missing closing bracket encountered in " + Expression);
            }
        } else {
            if (Expression.endsWith("]")) {
                throw new IllegalArgumentException("Unexpected closing bracket encountered in " + Expression);
            }
            // ReSharper disable once NotAccessedVariable
            /*
             * If this is a simple number, we can short-circuit the rest of this function
             */
            if (CSUtil.tryParseDouble(Expression)) {
                return Expression;
            }
            for (int j = 0; j < Expression.length(); j++) {
                if (!Character.isLetterOrDigit(Expression.charAt(j)) && Expression.charAt(j) != ':') {
                    throw new IllegalArgumentException("Invalid character encountered in variable " + Expression);
                }
            }
            return Expression;
        }
        String f = CSUtil.substring(Expression, 0, i);
        String s = CSUtil.substring(Expression, i + 1, Expression.length() - i - 2);
        String[] a = new String[4];
        int n = 0;
        int b = 0;
        for (i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '[': {
                    i++;
                    int m = 1;
                    boolean q = false;
                    while (i < s.length()) {
                        switch (s.charAt(i)) {
                            case '[':
                                m++;
                                break;
                            case ']':
                                m--;
                                if (m < 0) {
                                    throw new IllegalArgumentException("Unexpected closing bracket encountered in " + Expression);
                                }
                                if (m == 0) {
                                    q = true;
                                }
                                break;
                        }
                        if (q) {
                            break;
                        }
                        i++;
                    }
                    if (!q) {
                        throw new IllegalArgumentException("No closing bracket found in " + Expression);
                    }
                }
                break;
                case ']':
                    throw new IllegalArgumentException("Unexpected closing bracket encountered in " + Expression);
                case ',':
                    if (n == a.length) {
                        a = Arrays.copyOf(a, n << 1);
                    }
                    a[n] = CSUtil.substring(s, b, i - b).trim();
                    n++;
                    b = i + 1;
                    break;
            }
        }
        if (n == a.length) {
            a = Arrays.copyOf(a, n << 1);
        }
        a[n] = s.substring(b).trim();
        n++;
        if (n == 1 & a[0].length() == 0) {
            n = 0;
        }
        for (i = 0; i < n; i++) {
            if (a[i].length() == 0) {
                throw new IllegalArgumentException("An empty argument is invalid in " + f + " in " + Expression);
            }
            if (a[i].indexOf(' ') >= 0) {
                throw new IllegalArgumentException("An argument containing a space is invalid in " + f + " in " + Expression);
            }
            a[i] = GetPostfixNotationFromFunctionNotation(a[i]).trim();
        }
        switch (f.toLowerCase(Locale.ROOT)) {
            // arithmetic
            case "plus":
                switch (n) {
                    case 0:
                        return "0";
                    case 1:
                        return a[0];
                    case 2:
                        if (a[1].endsWith(" *")) {
                            return a[1] + " " + a[0] + " +";
                        } else {
                            return a[0] + " " + a[1] + " +";
                        }
                    default:
                        StringBuilder t = new StringBuilder(a[0] + " " + a[1] + " +");
                        for (i = 2; i < n; i++) {
                            t.append(" ").append(a[i]).append(" +");
                        }
                        return t.toString();
                }
            case "subtract":
                if (n == 2) {
                    return a[0] + " " + a[1] + " -";
                }
                throw new IllegalArgumentException(f + " is expected to have 2 arguments in " + Expression);
            case "times":
                switch (n) {
                    case 0:
                        return "1";
                    case 1:
                        return a[0];
                    case 2:
                        return a[0] + " " + a[1] + " *";
                    default:
                        StringBuilder t = new StringBuilder(a[0] + " " + a[1] + " *");
                        for (i = 2; i < n; i++) {
                            t.append(" ").append(a[i]).append(" *");
                        }
                        return t.toString();
                }
            case "divide":
                if (n == 2) {
                    return a[0] + " " + a[1] + " /";
                }
                throw new IllegalArgumentException(f + " is expected to have 2 arguments in " + Expression);
            case "power":
                switch (n) {
                    case 0:
                        return "1";
                    case 1:
                        return a[0];
                    case 2:
                        return a[0] + " " + a[1] + " power";
                    default:
                        StringBuilder t = new StringBuilder(a[0] + " " + a[1]);
                        for (i = 2; i < n; i++) {
                            t.append(" ").append(a[i]);
                        }
                        for (i = 0; i < n - 1; i++) {
                            t.append(" power");
                        }
                        return t.toString();
                }
                // math
            case "random":
            case "randomint":
                if (n == 2) {
                    return a[0] + " " + a[1] + " " + f;
                }
                throw new IllegalArgumentException(f + " is expected to have 2 arguments in " + Expression);
            case "quotient":
            case "mod":
            case "min":
            case "max":
                if (n == 2) {
                    return a[0] + " " + a[1] + " " + f;
                }
                throw new IllegalArgumentException(f + " is expected to have 2 arguments in " + Expression);
            case "minus":
            case "reciprocal":
            case "floor":
            case "ceiling":
            case "round":
            case "abs":
            case "sign":
            case "exp":
            case "log":
            case "sqrt":
            case "sin":
            case "cos":
            case "tan":
            case "arctan":
                if (n == 1) {
                    return a[0] + " " + f;
                }
                throw new IllegalArgumentException(f + " is expected to have 1 argument in " + Expression);
                // comparisons
            case "equal":
            case "unequal":
            case "less":
            case "greater":
            case "lessequal":
            case "greaterequal":
                if (n == 2) {
                    String g;
                    switch (f.toLowerCase(Locale.ROOT)) {
                        case "equal":
                            g = "==";
                            break;
                        case "unequal":
                            g = "!=";
                            break;
                        case "less":
                            g = "<";
                            break;
                        case "greater":
                            g = ">";
                            break;
                        case "lessequal":
                            g = "<=";
                            break;
                        case "greaterequal":
                            g = ">=";
                            break;
                        default:
                            g = "halt";
                            break;
                    }
                    return a[0] + " " + a[1] + " " + g;
                }
                throw new IllegalArgumentException(f + " is expected to have 2 arguments in " + Expression);
            case "if":
                if (n == 3) {
                    return a[0] + " " + a[1] + " " + a[2] + " ?";
                }
                throw new IllegalArgumentException(f + " is expected to have 3 arguments in " + Expression);
                // logical
            case "not":
                if (n == 1) {
                    return a[0] + " !";
                }
                throw new IllegalArgumentException(f + " is expected to have 1 argument in " + Expression);
            case "and":
                switch (n) {
                    case 0:
                        return "1";
                    case 1:
                        return a[0];
                    case 2:
                        return a[0] + " " + a[1] + " &";
                    default:
                        StringBuilder t = new StringBuilder(a[0] + " " + a[1] + " +");
                        for (i = 2; i < n; i++) {
                            t.append(" ").append(a[i]).append(" &");
                        }
                        return t.toString();
                }
            case "or":
                switch (n) {
                    case 0:
                        return "0";
                    case 1:
                        return a[0];
                    case 2:
                        return a[0] + " " + a[1] + " |";
                    default:
                        StringBuilder t = new StringBuilder(a[0] + " " + a[1] + " +");
                        for (i = 2; i < n; i++) {
                            t.append(" ").append(a[i]).append(" |");
                        }
                        return t.toString();
                }
            case "xor":
                switch (n) {
                    case 0:
                        return "0";
                    case 1:
                        return a[0];
                    case 2:
                        return a[0] + " " + a[1] + " ^";
                    default:
                        StringBuilder t = new StringBuilder(a[0] + " " + a[1] + " +");
                        for (i = 2; i < n; i++) {
                            t.append(" ").append(a[i]).append(" ^");
                        }
                        return t.toString();
                }
                // train
            case "distance":
            case "trackdistance":
            case "curveradius":
            case "frontaxlecurveradius":
            case "rearaxlecurveradius":
            case "curvecant":
            case "pitch":
            case "odometer":
            case "speed":
            case "speedometer":
            case "acceleration":
            case "accelerationmotor":
            case "doors":
            case "leftdoors":
            case "rightdoorstarget":
            case "leftdoorstarget":
            case "rightdoors":
            case "mainreservoir":
            case "equalizingreservoir":
            case "brakepipe":
            case "brakecylinder":
            case "straightairpipe":
                // station index functions
            case "distancestation":
            case "stopsstation":
                if (n == 1) {
                    return a[0] + " " + f.toLowerCase(Locale.ROOT) + "index";
                }
                throw new IllegalArgumentException(f + " is expected to have 1 argument in " + Expression);
            case "pluginstate":
                if (n == 1) {
                    return a[0] + " pluginstate";
                }
                throw new IllegalArgumentException(f + " is expected to have 1 argument in " + Expression);
                // not supported
            default:
                throw new IllegalArgumentException("The function " + f + " is not supported in " + Expression);
        }
    }

    /// <summary>Converts a String formatted in infix notation to postfix notation</summary>
    /// <param name="Expression">The function script String</param>
    public static String GetPostfixNotationFromInfixNotation(String Expression) {
        String Function = Notation.GetFunctionNotationFromInfixNotation(Expression, true);
        return GetPostfixNotationFromFunctionNotation(Function);
    }

    /// <summary>Gets the optimized version of a postfix notated function script</summary>
    /// <param name="Expression">The function script String to optimize</param>

    static String GetOptimizedPostfixNotation(String Expression) {
        Expression = " " + Expression + " ";
        Expression = Expression.replace(" 1 1 == -- ", " 0 ");
        Expression = Expression.replace(" 1 doors - 1 == -- ", " doors ! -- ");
        String[] Arguments = Expression.trim().split("\\s+");
        String[] Stack = new String[Arguments.length];
        int StackLength = 0;
        for (int i = 0; i < Arguments.length; i++) {
            switch (Arguments[i].toLowerCase(Locale.ROOT)) {
                case "<>": {
                    boolean q = true;
                    if (StackLength >= 1) {
                        if (Objects.equals(Stack[StackLength - 1], "<>")) {
                            // <> <>
                            // [n/a]
                            StackLength--;
                            q = false;
                        } else if (StackLength >= 2) {
                            // ReSharper disable once NotAccessedVariable
                            if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                                // ReSharper disable once NotAccessedVariable
                                if (CSUtil.tryParseDouble(Stack[StackLength - 2])) {
                                    // a b <>
                                    // b a
                                    String t = Stack[StackLength - 1];
                                    Stack[StackLength - 1] = Stack[StackLength - 2];
                                    Stack[StackLength - 2] = t;
                                }
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "+": {
                    boolean q = true;
                    if (StackLength >= 2) {
                        double b;
                        if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                            b = Double.parseDouble(Stack[StackLength - 1]);
                            double a;
                            if (CSUtil.tryParseDouble(Stack[StackLength - 2])) {
                                a = Double.parseDouble(Stack[StackLength - 2]);
                                // x y +
                                // (x y +)
                                Stack[StackLength - 2] = Double.toString((a + b));
                                StackLength--;
                                q = false;
                            } else if (StackLength >= 3 && Objects.equals(Stack[StackLength - 2], "+")) {
                                if (CSUtil.tryParseDouble(Stack[StackLength - 3])) {
                                    a = Double.parseDouble(Stack[StackLength - 3]);
                                    // A x + y +
                                    // A (y x +) +
                                    Stack[StackLength - 3] = Double.toString((a + b));
                                    StackLength--;
                                    q = false;
                                }
                            } else if (StackLength >= 3 && Objects.equals(Stack[StackLength - 2], "-")) {
                                if (CSUtil.tryParseDouble(Stack[StackLength - 3])) {
                                    a = Double.parseDouble(Stack[StackLength - 3]);
                                    // A x - y +
                                    // A (y x -) +
                                    Stack[StackLength - 3] = Double.toString((b - a));
                                    Stack[StackLength - 2] = "+";
                                    StackLength--;
                                    q = false;
                                }
                            } else if (Objects.equals(Stack[StackLength - 2], "*")) {
                                // A x * y +
                                // A x y fma
                                Stack[StackLength - 2] = Stack[StackLength - 1];
                                Stack[StackLength - 1] = "fma";
                                q = false;
                            } else if (Objects.equals(Stack[StackLength - 2], "fma")) {
                                if (CSUtil.tryParseDouble(Stack[StackLength - 3])) {
                                    a = Double.parseDouble(Stack[StackLength - 3]);
                                    // A B y fma z +
                                    // A B (y z +) fma
                                    Stack[StackLength - 3] = Double.toString((a + b));
                                    StackLength--;
                                    q = false;
                                }
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "-": {
                    boolean q = true;
                    if (StackLength >= 2) {
                        double b;
                        if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                            b = Double.parseDouble(Stack[StackLength - 1]);
                            double a;
                            if (CSUtil.tryParseDouble(Stack[StackLength - 2])) {
                                a = Double.parseDouble(Stack[StackLength - 2]);
                                // x y -
                                // (x y -)
                                Stack[StackLength - 2] = Double.toString((a - b));
                                StackLength--;
                                q = false;
                            } else if (StackLength >= 3 && Objects.equals(Stack[StackLength - 2], "+")) {
                                if (CSUtil.tryParseDouble(Stack[StackLength - 3])) {
                                    a = Double.parseDouble(Stack[StackLength - 3]);
                                    // A x + y -
                                    // A (x y -) +
                                    Stack[StackLength - 3] = Double.toString((a - b));
                                    Stack[StackLength - 2] = "+";
                                    StackLength--;
                                    q = false;
                                }
                            } else if (StackLength >= 3 && Objects.equals(Stack[StackLength - 2], "-")) {
                                if (CSUtil.tryParseDouble(Stack[StackLength - 3])) {
                                    a = Double.parseDouble(Stack[StackLength - 3]);
                                    // A x - y -
                                    // A (x y + minus) -
                                    Stack[StackLength - 3] = Double.toString((-a - b));
                                    Stack[StackLength - 2] = "+";
                                    StackLength--;
                                    q = false;
                                }
                            } else if (Objects.equals(Stack[StackLength - 2], "*")) {
                                // A x * y -
                                // A x (y minus) fma
                                Stack[StackLength - 2] = Double.toString((-b));
                                Stack[StackLength - 1] = "fma";
                                q = false;
                            } else if (Objects.equals(Stack[StackLength - 2], "fma")) {
                                if (CSUtil.tryParseDouble(Stack[StackLength - 3])) {
                                    a = Double.parseDouble(Stack[StackLength - 3]);
                                    // A B y fma z -
                                    // A B (y z -) fma
                                    Stack[StackLength - 3] = Double.toString((a - b));
                                    StackLength--;
                                    q = false;
                                }
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "minus": {
                    boolean q = true;
                    if (StackLength >= 1) {
                        if (Stack[StackLength - 1].equalsIgnoreCase("minus")) {
                            // minus minus
                            // [n/a]
                            StackLength--;
                            q = false;
                        } else {
                            double a;
                            if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                                a = Double.parseDouble(Stack[StackLength - 1]);
                                // x minus
                                // (x minus)
                                Stack[StackLength - 1] = Double.toString((-a));
                                q = false;
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "*": {
                    boolean q = true;
                    if (StackLength >= 2) {
                        double b;
                        if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                            b = Double.parseDouble(Stack[StackLength - 1]);
                            double a;
                            if (CSUtil.tryParseDouble(Stack[StackLength - 2])) {
                                a = Double.parseDouble(Stack[StackLength - 2]);
                                // x y *
                                // (x y *)
                                Stack[StackLength - 2] = Double.toString((a * b));
                                StackLength--;
                                q = false;
                            } else if (StackLength >= 3 && Objects.equals(Stack[StackLength - 2], "*")) {
                                if (CSUtil.tryParseDouble(Stack[StackLength - 3])) {
                                    a = Double.parseDouble(Stack[StackLength - 3]);
                                    // A x * y *
                                    // A (x y *) *
                                    Stack[StackLength - 3] = Double.toString((a * b));
                                    StackLength--;
                                    q = false;
                                }
                            } else if (StackLength >= 3 && Objects.equals(Stack[StackLength - 2], "+")) {
                                if (CSUtil.tryParseDouble(Stack[StackLength - 3])) {
                                    a = Double.parseDouble(Stack[StackLength - 3]);
                                    // A x + y *
                                    // A y (x y *) fma
                                    Stack[StackLength - 3] = Stack[StackLength - 1];
                                    Stack[StackLength - 2] = Double.toString((a * b));
                                    Stack[StackLength - 1] = "fma";
                                    q = false;
                                }
                            } else if (StackLength >= 3 && Objects.equals(Stack[StackLength - 2], "-")) {
                                if (CSUtil.tryParseDouble(Stack[StackLength - 3])) {
                                    a = Double.parseDouble(Stack[StackLength - 3]);
                                    // A x - y *
                                    // A y (x y * minus) fma
                                    Stack[StackLength - 3] = Stack[StackLength - 1];
                                    Stack[StackLength - 2] = Double.toString((-a * b));
                                    Stack[StackLength - 1] = "fma";
                                    q = false;
                                }
                            } else if (StackLength >= 4 && Objects.equals(Stack[StackLength - 2], "fma")) {
                                if (CSUtil.tryParseDouble(Stack[StackLength - 3])) {
                                    a = Double.parseDouble(Stack[StackLength - 3]);
                                    double c;
                                    if (CSUtil.tryParseDouble(Stack[StackLength - 4])) {
                                        c = Double.parseDouble(Stack[StackLength - 4]);
                                        // A x y fma z *
                                        // A (x z *) (y z *) fma
                                        Stack[StackLength - 4] = Double.toString((c * b));
                                        Stack[StackLength - 3] = Double.toString((a * b));
                                        StackLength--;

                                        q = false;
                                    } else {
                                        // A B y fma z *
                                        // A B * z (y z *) fma
                                        Stack[StackLength - 3] = "*";
                                        Stack[StackLength - 2] = Stack[StackLength - 1];
                                        Stack[StackLength - 1] = Double.toString((a * b));
                                        Stack[StackLength] = "fma";
                                        StackLength++;
                                        q = false;
                                    }
                                }
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "reciprocal": {
                    boolean q = true;
                    if (StackLength >= 1) {
                        if (Stack[StackLength - 1].equalsIgnoreCase("reciprocal")) {
                            // reciprocal reciprocal
                            // [n/a]
                            StackLength--;
                            q = false;
                        } else {
                            double a;
                            if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                                a = Double.parseDouble(Stack[StackLength - 1]);
                                // x reciprocal
                                // (x reciprocal)
                                a = a == 0.0 ? 0.0 : 1.0 / a;
                                Stack[StackLength - 1] = Double.toString(a);
                                q = false;
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "/": {
                    boolean q = true;
                    if (StackLength >= 2) {
                        double b;
                        if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                            b = Double.parseDouble(Stack[StackLength - 1]);
                            if (b != 0.0) {
                                double a;
                                if (CSUtil.tryParseDouble(Stack[StackLength - 2])) {
                                    a = Double.parseDouble(Stack[StackLength - 2]);
                                    // x y /
                                    // (x y /)
                                    Stack[StackLength - 2] = Double.toString((a / b));
                                    StackLength--;
                                    q = false;
                                } else if (StackLength >= 3 && Objects.equals(Stack[StackLength - 2], "*")) {
                                    if (CSUtil.tryParseDouble(Stack[StackLength - 3])) {
                                        a = Double.parseDouble(Stack[StackLength - 3]);
                                        // A x * y /
                                        // A (x y /) *
                                        Stack[StackLength - 3] = Double.toString((a / b));
                                        StackLength--;
                                        q = false;
                                    }
                                }
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "++": {
                    boolean q = true;
                    if (StackLength >= 1) {
                        double a;
                        if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                            a = Double.parseDouble(Stack[StackLength - 1]);
                            // x ++
                            // (x ++)
                            Stack[StackLength - 1] = Double.toString((a + 1));
                            q = false;
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "--": {
                    boolean q = true;
                    if (StackLength >= 1) {
                        double a;
                        if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                            a = Double.parseDouble(Stack[StackLength - 1]);
                            // x --
                            // (x --)
                            Stack[StackLength - 1] = Double.toString((a - 1));
                            q = false;
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "!": {
                    boolean q = true;
                    if (StackLength >= 1) {
                        if (Objects.equals(Stack[StackLength - 1], "!")) {
                            StackLength--;
                            q = false;
                        } else if (Objects.equals(Stack[StackLength - 1], "==")) {
                            Stack[StackLength - 1] = "!=";
                            q = false;
                        } else if (Objects.equals(Stack[StackLength - 1], "!=")) {
                            Stack[StackLength - 1] = "==";
                            q = false;
                        } else if (Objects.equals(Stack[StackLength - 1], "<")) {
                            Stack[StackLength - 1] = ">=";
                            q = false;
                        } else if (Objects.equals(Stack[StackLength - 1], ">")) {
                            Stack[StackLength - 1] = "<=";
                            q = false;
                        } else if (Objects.equals(Stack[StackLength - 1], "<=")) {
                            Stack[StackLength - 1] = ">";
                            q = false;
                        } else if (Objects.equals(Stack[StackLength - 1], ">=")) {
                            Stack[StackLength - 1] = "<";
                            q = false;
                        } else {
                            double a;
                            if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                                a = Double.parseDouble(Stack[StackLength - 1]);
                                Stack[StackLength - 1] = a == 0.0 ? "1" : "0";
                                q = false;
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "==": {
                    boolean q = true;
                    if (StackLength >= 2) {
                        double b;
                        if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                            b = Double.parseDouble(Stack[StackLength - 1]);
                            double a;
                            if (CSUtil.tryParseDouble(Stack[StackLength - 2])) {
                                a = Double.parseDouble(Stack[StackLength - 2]);
                                Stack[StackLength - 2] = a == b ? "1" : "0";
                                StackLength--;
                                q = false;
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "!=": {
                    boolean q = true;
                    if (StackLength >= 2) {
                        double b;
                        if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                            b = Double.parseDouble(Stack[StackLength - 1]);
                            double a;
                            if (CSUtil.tryParseDouble(Stack[StackLength - 2])) {
                                a = Double.parseDouble(Stack[StackLength - 2]);
                                Stack[StackLength - 2] = a != b ? "1" : "0";
                                StackLength--;
                                q = false;
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "<": {
                    boolean q = true;
                    if (StackLength >= 2) {
                        double b;
                        if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                            b = Double.parseDouble(Stack[StackLength - 1]);
                            double a;
                            if (CSUtil.tryParseDouble(Stack[StackLength - 2])) {
                                a = Double.parseDouble(Stack[StackLength - 2]);
                                Stack[StackLength - 2] = a < b ? "1" : "0";
                                StackLength--;
                                q = false;
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case ">": {
                    boolean q = true;
                    if (StackLength >= 2) {
                        double b;
                        if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                            b = Double.parseDouble(Stack[StackLength - 1]);
                            double a;
                            if (CSUtil.tryParseDouble(Stack[StackLength - 2])) {
                                a = Double.parseDouble(Stack[StackLength - 2]);
                                Stack[StackLength - 2] = a > b ? "1" : "0";
                                StackLength--;
                                q = false;
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "<=": {
                    boolean q = true;
                    if (StackLength >= 2) {
                        double b;
                        if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                            b = Double.parseDouble(Stack[StackLength - 1]);
                            double a;
                            if (CSUtil.tryParseDouble(Stack[StackLength - 2])) {
                                a = Double.parseDouble(Stack[StackLength - 2]);
                                Stack[StackLength - 2] = a <= b ? "1" : "0";
                                StackLength--;
                                q = false;
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case ">=": {
                    boolean q = true;
                    if (StackLength >= 2) {
                        double b;
                        if (CSUtil.tryParseDouble(Stack[StackLength - 1])) {
                            b = Double.parseDouble(Stack[StackLength - 1]);
                            double a;
                            if (CSUtil.tryParseDouble(Stack[StackLength - 2])) {
                                a = Double.parseDouble(Stack[StackLength - 2]);
                                Stack[StackLength - 2] = a >= b ? "1" : "0";
                                StackLength--;
                                q = false;
                            }
                        }
                    }
                    if (q) {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                }
                break;
                case "floor":
                    if (StackLength >= 1 && Objects.equals(Stack[StackLength - 1], "/")) {
                        Stack[StackLength - 1] = "quotient";
                    } else {
                        Stack[StackLength] = Arguments[i];
                        StackLength++;
                    }
                    break;
                default:
                    Stack[StackLength] = Arguments[i];
                    StackLength++;
                    break;
            }
        }

        StringBuilder Builder = new StringBuilder();
        for (
                int i = 0;
                i < StackLength; i++) {
            if (i != 0) Builder.append(' ');
            Builder.append(Stack[i]);
        }
        return Builder.toString();
    }

    /// <summary>Converts a String in infix function notation into simple function script</summary>
    /// <param name="Expression">The function script String</param>
    /// <param name="Preprocessing">Whether this is preprocessing</param>
    /// <returns>The simple function script String</returns>
    public static String GetFunctionNotationFromInfixNotation(String Expression, boolean Preprocessing) {
        // brackets
        if (Preprocessing) {
            int s = 0;
            while (true) {
                if (s >= Expression.length()) break;
                int i = Expression.indexOf('[', s);
                if (i >= s) {
                    int j = i + 1, t = j, m = 1;
                    String[] p = new String[4];
                    int n = 0;
                    boolean q = false;
                    while (j < Expression.length()) {
                        switch (Expression.charAt(j)) {
                            case '[':
                                m++;
                                break;
                            case ']':
                                m--;
                                if (m < 0) {
                                    throw new IllegalArgumentException("Unexpected closing bracket encountered in " + Expression);
                                }
                                if (m == 0) {
                                    if (n >= p.length) p = Arrays.copyOf(p, n << 1);
                                    p[n] = CSUtil.substring(Expression, t, j - t);
                                    n++;
                                    String a = CSUtil.substring(Expression, 0, i).trim();
                                    String c = Expression.substring(j + 1).trim();
                                    StringBuilder r = new StringBuilder();
                                    for (int k = 0; k < n; k++) {
                                        p[k] = GetFunctionNotationFromInfixNotation(p[k], true);
                                        if (k > 0) r.append(',');
                                        r.append(p[k]);
                                    }
                                    Expression = a + "[" + r + "]" + c;
                                    s = i + r.length() + 2;
                                    q = true;
                                }
                                break;
                            case ',':
                                if (m == 1) {
                                    if (n >= p.length) p = Arrays.copyOf(p, n << 1);
                                    p[n] = CSUtil.substring(Expression, t, j - t);
                                    n++;
                                    t = j + 1;
                                }
                                break;
                        }
                        if (q) {
                            break;
                        }
                        j++;
                    }
                    if (!q) {
                        throw new IllegalArgumentException("Missing closing bracket encountered in " + Expression);
                    }
                } else {
                    break;
                }
            }
        }
        // parentheses
        {
            int i = Expression.indexOf('(');
            if (i >= 0) {
                int j = i + 1;
                int n = 1;
                while (j < Expression.length()) {
                    switch (Expression.charAt(j)) {
                        case '(':
                            n++;
                            break;
                        case ')':
                            n--;
                            if (n < 0) {
                                throw new IllegalArgumentException("Unexpected closing parenthesis encountered in " + Expression);
                            }
                            if (n == 0) {
                                String a = CSUtil.substring(Expression, 0, i).trim();
                                String b = CSUtil.substring(Expression, i + 1, j - i - 1).trim();
                                String c = Expression.substring(j + 1).trim();
                                return GetFunctionNotationFromInfixNotation(a + GetFunctionNotationFromInfixNotation(b, false) + c,
                                        false);
                            }
                            break;
                    }
                    j++;
                }
                throw new IllegalArgumentException("No closing parenthesis found in " + Expression);
            } else {
                i = Expression.indexOf(')');
                if (i >= 0) {
                    throw new IllegalArgumentException("Unexpected closing parenthesis encountered in " + Expression);
                }
            }
        }
        // operators
        {
            int i = Expression.indexOf('|');
            if (i >= 0) {
                String a = CSUtil.substring(Expression, 0, i).trim();
                String b = Expression.substring(i + 1).trim();
                return "Or[" + GetFunctionNotationFromInfixNotation(a, false) + "," + GetFunctionNotationFromInfixNotation(b, false) + "]";
            }
        }
        {
            int i = Expression.indexOf('^');
            if (i >= 0) {
                String a = CSUtil.substring(Expression, 0, i).trim();
                String b = Expression.substring(i + 1).trim();
                return "Xor[" + GetFunctionNotationFromInfixNotation(a, false) + "," + GetFunctionNotationFromInfixNotation(b, false) + "]";
            }
        }
        {
            int i = Expression.indexOf('&');
            if (i >= 0) {
                String a = CSUtil.substring(Expression, 0, i).trim();
                String b = Expression.substring(i + 1).trim();
                return "And[" + GetFunctionNotationFromInfixNotation(a, false) + "," + GetFunctionNotationFromInfixNotation(b, false) + "]";
            }
        }
        {
            int i = Expression.indexOf('!');
            while (true) {
                if (i >= 0) {
                    if (i < Expression.length() - 1) {
                        if (Expression.charAt(i + 1) == '=') {
                            int j = Expression.indexOf('!', i + 2);
                            i = j < i + 2 ? -1 : j;
                        } else break;
                    } else break;
                } else break;
            }
            if (i >= 0) {
                String b = Expression.substring(i + 1).trim();
                return "Not[" + GetFunctionNotationFromInfixNotation(b, false) + "]";
            }
        }
        {
            int[] j = new int[6];
            j[0] = Expression.lastIndexOf("==");
            j[1] = Expression.lastIndexOf("!=");
            j[2] = Expression.lastIndexOf("<=");
            j[3] = Expression.lastIndexOf(">=");
            j[4] = Expression.lastIndexOf("<");
            j[5] = Expression.lastIndexOf(">");
            int k = -1;
            for (int i = 0; i < j.length; i++) {
                if (j[i] >= 0) {
                    if (k >= 0) {
                        if (j[i] > j[k]) k = i;
                    } else {
                        k = i;
                    }
                }
            }
            if (k >= 0) {
                int l = k <= 3 ? 2 : 1;
                String a = CSUtil.substring(Expression, 0, j[k]).trim();
                String b = Expression.substring(j[k] + l).trim();
                String f;
                switch (k) {
                    case 0:
                        f = "Equal";
                        break;
                    case 1:
                        f = "Unequal";
                        break;
                    case 2:
                        f = "LessEqual";
                        break;
                    case 3:
                        f = "GreaterEqual";
                        break;
                    case 4:
                        f = "Less";
                        break;
                    case 5:
                        f = "Greater";
                        break;
                    default:
                        f = "Halt";
                        break;
                }
                return f + "[" + GetFunctionNotationFromInfixNotation(a, false) + "," + GetFunctionNotationFromInfixNotation(b, false) + "]";
            }
        }
        {
            int i = Expression.lastIndexOf('+');
            int j = Expression.lastIndexOf('-');
            if (i >= 0 & (j == -1 | j >= 0 & i > j)) {
                String a = CSUtil.substring(Expression, 0, i).trim();
                String b = Expression.substring(i + 1).trim();
                return "Plus[" + GetFunctionNotationFromInfixNotation(a, false) + "," + GetFunctionNotationFromInfixNotation(b, false) + "]";
            } else if (j >= 0) {
                String a = CSUtil.substring(Expression, 0, j).trim();
                String b = Expression.substring(j + 1).trim();
                if (a.length() != 0) {
                    return "Subtract[" + GetFunctionNotationFromInfixNotation(a, false) + "," + GetFunctionNotationFromInfixNotation(b, false) + "]";
                }
            }
        }
        {
            int i = Expression.indexOf('*');
            if (i >= 0) {
                String a = CSUtil.substring(Expression, 0, i).trim();
                String b = Expression.substring(i + 1).trim();
                return "Times[" + GetFunctionNotationFromInfixNotation(a, false) + "," + GetFunctionNotationFromInfixNotation(b, false) + "]";
            }
        }
        {
            int i = Expression.indexOf('/');
            if (i >= 0) {
                String a = CSUtil.substring(Expression, 0, i).trim();
                String b = Expression.substring(i + 1).trim();
                return "Divide[" + GetFunctionNotationFromInfixNotation(a, false) + "," + GetFunctionNotationFromInfixNotation(b, false) + "]";
            }
        }
        {
            int i = Expression.indexOf('-');
            if (i >= 0) {
                String a = CSUtil.substring(Expression, 0, i).trim();
                String b = Expression.substring(i + 1).trim();
                if (a.length() == 0) {
                    return "Minus[" + GetFunctionNotationFromInfixNotation(b, false) + "]";
                }
            }
        }
        return Expression.trim();
    }
}
