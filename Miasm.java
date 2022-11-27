package miasm;

import java.util.*;

/**
 *
 * @author Yahia
 */
public class Miasm {

    /**
     * @param args the command line arguments
     */
    static List<String> names = new ArrayList<>();
    static List<Integer> adresses = new ArrayList<>();
    static List<Integer> values = new ArrayList<>();
    static Map<String, Integer> etiquettes = new HashMap<>();
    static int errCode = -10101011;
    static int init = 0;
    static int acc = 0;
    static int currAddress = 0;
    static boolean stop = false;
    static String[] keywords = {"ORG", "RC", "RM", "DEBUT", "RGM", "CHM",
        "CHM", "CHI", "ADI", "ADM", "SI", "SM", "ET", "OU", "OUX", "NON", "BCV0", "BCV1",
        "BCV2", "BCV3", "BCV4", "BCF0", "BCF1", "BCF2", "BCF3", "BCF4", "ENT",
        "ENT", "SOR", "STOP", "END"};
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        String instructions ="ORG X'100';"
                + "A RC X'100' 100 3;"
                + "B RC 6;"
                + "DEBUT;"
                + "BOUCLE CHM *A;"
                + "SOR 02;"
                + "STOP;"
                + "END";
        instructions = normalised(instructions);
        String[] ar = instructions.split(" ");
        check(ar);
        if (!stop) {
            setEtiquttes(ar);
            start(ar);
        }

    }

    static boolean isKey(String s) {
        for (int i = 0; i < keywords.length; i++) {
            if (keywords[i].equals(s)) {
                return true;
            }
        }
        return false;
    }

    static boolean isCondition(String s) {
        for (int i = 16; i <= 25; i++) {
            if (keywords[i].equals(s)) {
                return true;
            }
        }
        return false;
    }

    static void setEtiquttes(String[] p) {
        for (int i = 0; i < p.length; i++) {
            if (p[i].equals("DEBUT") || !isKey(p[i])) {
                if (!isCondition(p[i - 1])) {
                    etiquettes.put(p[i], i);
                }
            }
        }
    }

    static void start(String[] s) {
        for (int i = 0; i < s.length; i++) {
            if (!s[i].equals("DEBUT")) {
                switch (s[i]) {
                    case "ORG":
                        currAddress = value(s[i + 1]);
                        break;
                    case "RM":
                        rangeVariables(s[i - 1], s[i + 1]);
                        break;
                    case "RC":
                        setValues(s[i - 1], s, i + 1);
                        break;
                }
            } else {
                execute(s, i + 1);
                return;
            }
        }
    }

    static void execute(String[] p, int ind) {
        for (int i = 0; i < p.length; i++) {
            switch (p[i]) {
                case "CHM":
                    acc = GetValueFromAddress(value(p[i + 1]));
                    break;
                case "CHI":
                    acc = value(p[i + 1]);
                    break;
                case "ADM":
                    acc = acc + GetValueFromAddress(value(p[i + 1]));
                    break;
                case "ADI":
                    acc = acc + value(p[i + 1]);
                    break;
                case "SM":
                    acc = acc - GetValueFromAddress(value(p[i + 1]));
                    break;
                case "SI":
                    acc = acc - value(p[i + 1]);
                    break;
                case "ET":
                    acc = acc & GetValueFromAddress(value(p[i + 1]));
                    break;
                case "OUX":
                    acc = acc ^ GetValueFromAddress(value(p[i + 1]));
                    break;
                case "NON":
                    acc = ~acc;
                    break;
                case "RGM":
                    for (int a = 0; a < adresses.size(); a++) {
                        if (adresses.get(a) == value(p[a + 1])) {
                            values.set(a, acc);
                            break;
                        }
                    }
                    break;
                case "BCV0":
                    i = etiquettes.get(p[i + 1]) - 1;
                    break;
                case "BCV1":
                    if (acc > Math.pow(2, 15) && acc < Math.pow(2, 16)) {
                        i = etiquettes.get(p[i + 1]) - 1;
                    }
                    break;
                case "BCV2":
                    if (acc > Math.pow(2, 16)) {
                        i = etiquettes.get(p[i + 1]) - 1;
                    }
                    break;
                case "BCV3":
                    if (acc == 0) {
                        i = etiquettes.get(p[i + 1]) - 1;
                    }
                    break;
                case "BCV4":
                    if (acc < 0) {
                        i = etiquettes.get(p[i + 1]) - 1;
                    }
                    break;
                case "BCF0":
                    i = etiquettes.get(p[i + 1]) - 1;
                    break;
                case "BCF1":
                    if (!(acc > Math.pow(2, 15) && acc < Math.pow(2, 16))) {
                        i = etiquettes.get(p[i + 1]) - 1;
                    }
                    break;
                case "BCF2":
                    if (acc <= Math.pow(2, 16)) {
                        i = etiquettes.get(p[i + 1]) - 1;
                    }
                    break;
                case "BCF3":
                    if (acc != 0) {
                        i = etiquettes.get(p[i + 1]) - 1;
                    }
                    break;
                case "BCF4":
                    if (acc >= 0) {
                        i = etiquettes.get(p[i + 1]) - 1;
                    }
                    break;
                case "ENT":
                    acc = sc.nextInt();
                    break;
                case "SOR":
                    System.out.println(acc);
                    break;
                case "STOP":
                    //  System.exit(0);
                    break;
            }
        }
    }

    static void setValues(String name, String[] p, int ind) {
        while (!p[ind + 1].equals("RC") && !p[ind + 1].equals("RM") && !p[ind].equals("DEBUT")) {
            names.add(name);
            adresses.add(currAddress);
            values.add(value(p[ind]));
            currAddress++;
            ind++;
        }
    }

    static void rangeVariables(String name, String howmuch) {
        int c = value(howmuch);
        for (int i = 0; i < c; i++) {
            names.add(name);
            adresses.add(currAddress);
            //simulate initial value for RM it should be ``random`` sort to say...
            values.add(-Integer.MIN_VALUE + (int) (Math.random() * 2 * Integer.MAX_VALUE));
            currAddress++;
        }
    }

    static int value(String s) {
        int p = 1;
        int ans = 0;
        int len = s.length();
        if (isHexa(s)) {
            while (len > 0) {
                char toadd = s.charAt(len - 1);
                if (toadd <= 'F' && toadd >= 'A') {
                    ans += (int) (toadd - 'A' + 10) * p;
                    p = p * 16;
                } else if (Character.isDigit(toadd)) {
                    ans += (int) (toadd - '0') * p;
                    p = p * 16;
                }
                len--;
            }
            return ans;
        }
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
        }
        if (s.charAt(0) == '*') {
            return GetValueFromAddress(GetValueFromAddressWithName(s.substring(1)));
        }
        return GetValueFromAddressWithName(s);
    }

    static int GetValueFromAddress(int n) {
        for (int i = 0; i < adresses.size(); i++) {
            if (adresses.get(i) == n) {
                return values.get(i);
            }
        }
        return errCode;
    }

    static int GetValueFromAddressWithName(String s) {
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(s)) {
                return adresses.get(i);
            }
        }
        return errCode;
    }

    static boolean isHexa(String s) {
        return s.length() > 3 && s.charAt(0) == 'X' && s.charAt(1) == (char) 39 && s.charAt(s.length() - 1) == (char) 39;
    }

    static void check(String[] ar) {
        if (!contains(ar, "ORG")) {
            System.err.println("ORG expected");
            stop = true;
        }
        if (!contains(ar, "DEBUT")) {
            System.err.println("DEBUT expected");
            stop = true;
        }
        if (!contains(ar, "STOP")) {
            System.err.println("STOP expected");
            stop = true;
        }
        if (!contains(ar, "END")) {
            System.err.println("END expected");
            stop = true;
        }
    }

    static boolean contains(String[] ar, String w) {
        for (String s : ar) {
            if (s.equals(w)) {
                return true;
            }
        }
        return false;
    }

    static String normalised(String p) {
        p = p.trim();
        p = p.toUpperCase();
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < p.length(); i++) {
            if (p.charAt(i) == '*') {
                sb.append(p.charAt(i));
            }
            if (Character.isLetter(p.charAt(i)) || Character.isDigit(p.charAt(i)) || p.charAt(i) == (char) 39) {
                sb.append(p.charAt(i));
            }
            if ((p.charAt(i) == ' ' || p.charAt(i) == ';') && (Character.isLetter(sb.charAt(sb.length() - 1)) || Character.isDigit(sb.charAt(sb.length() - 1)) || sb.charAt(sb.length() - 1) == (char) 39)) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }
}
