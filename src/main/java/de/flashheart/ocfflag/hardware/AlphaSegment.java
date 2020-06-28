package de.flashheart.ocfflag.hardware;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AlphaSegment extends HT16K33 {
    public final static Map<String, Integer> ALPHA_SEG_CHARS = new HashMap<>();

    static {
        // Reverse engineered from Adafruit python library
        ALPHA_SEG_CHARS.put("  ", 16383);
        ALPHA_SEG_CHARS.put(" ", 2048);
        ALPHA_SEG_CHARS.put("!", 16390);
        ALPHA_SEG_CHARS.put("\"", 544);
        ALPHA_SEG_CHARS.put("#", 4814);
        ALPHA_SEG_CHARS.put("$", 4845);
        ALPHA_SEG_CHARS.put("%", 3108);
        ALPHA_SEG_CHARS.put("&", 9053);
        ALPHA_SEG_CHARS.put("'", 1024);
        ALPHA_SEG_CHARS.put("(", 9216);
        ALPHA_SEG_CHARS.put(")", 2304);
        ALPHA_SEG_CHARS.put("*", 16320);
        ALPHA_SEG_CHARS.put("+", 4800);
        ALPHA_SEG_CHARS.put("-", 192);
        ALPHA_SEG_CHARS.put(".", 0);
        ALPHA_SEG_CHARS.put("/", 3072);
        ALPHA_SEG_CHARS.put("0", 3135);
        ALPHA_SEG_CHARS.put("1", 6);
        ALPHA_SEG_CHARS.put("2", 219);
        ALPHA_SEG_CHARS.put("3", 143);
        ALPHA_SEG_CHARS.put("4", 230);
        ALPHA_SEG_CHARS.put("5", 8297);
        ALPHA_SEG_CHARS.put("6", 253);
        ALPHA_SEG_CHARS.put("7", 7);
        ALPHA_SEG_CHARS.put("8", 255);
        ALPHA_SEG_CHARS.put("9", 239);
        ALPHA_SEG_CHARS.put(":", 4608);
        ALPHA_SEG_CHARS.put(";", 2560);
        ALPHA_SEG_CHARS.put("<", 9280);
        ALPHA_SEG_CHARS.put("=", 200);
        ALPHA_SEG_CHARS.put(">", 2432);
        ALPHA_SEG_CHARS.put("?", 24739);
        ALPHA_SEG_CHARS.put("@", 699);
        ALPHA_SEG_CHARS.put("A", 247);
        ALPHA_SEG_CHARS.put("B", 4751);
        ALPHA_SEG_CHARS.put("C", 57);
        ALPHA_SEG_CHARS.put("D", 4623);
        ALPHA_SEG_CHARS.put("E", 249);
        ALPHA_SEG_CHARS.put("F", 113);
        ALPHA_SEG_CHARS.put("G", 189);
        ALPHA_SEG_CHARS.put("H", 246);
        ALPHA_SEG_CHARS.put("I", 4608);
        ALPHA_SEG_CHARS.put("J", 30);
        ALPHA_SEG_CHARS.put("K", 9328);
        ALPHA_SEG_CHARS.put("L", 56);
        ALPHA_SEG_CHARS.put("M", 1334);
        ALPHA_SEG_CHARS.put("N", 8502);
        ALPHA_SEG_CHARS.put("O", 63);
        ALPHA_SEG_CHARS.put("P", 243);
        ALPHA_SEG_CHARS.put("Q", 8255);
        ALPHA_SEG_CHARS.put("R", 8435);
        ALPHA_SEG_CHARS.put("S", 237);
        ALPHA_SEG_CHARS.put("T", 4609);
        ALPHA_SEG_CHARS.put("U", 62);
        ALPHA_SEG_CHARS.put("V", 3120);
        ALPHA_SEG_CHARS.put("W", 10294);
        ALPHA_SEG_CHARS.put("X", 11520);
        ALPHA_SEG_CHARS.put("Y", 5376);
        ALPHA_SEG_CHARS.put("Z", 3081);
        ALPHA_SEG_CHARS.put("[", 57);
        ALPHA_SEG_CHARS.put("\\", 8448);
        ALPHA_SEG_CHARS.put("]", 15);
        ALPHA_SEG_CHARS.put("^", 3075);
        ALPHA_SEG_CHARS.put("_", 8);
        ALPHA_SEG_CHARS.put("`", 256);
        ALPHA_SEG_CHARS.put("a", 4184);
        ALPHA_SEG_CHARS.put("b", 8312);
        ALPHA_SEG_CHARS.put("c", 216);
        ALPHA_SEG_CHARS.put("d", 2190);
        ALPHA_SEG_CHARS.put("e", 2136);
        ALPHA_SEG_CHARS.put("f", 113);
        ALPHA_SEG_CHARS.put("g", 1166);
        ALPHA_SEG_CHARS.put("h", 4208);
        ALPHA_SEG_CHARS.put("i", 4096);
        ALPHA_SEG_CHARS.put("j", 14);
        ALPHA_SEG_CHARS.put("k", 13824);
        ALPHA_SEG_CHARS.put("l", 48);
        ALPHA_SEG_CHARS.put("m", 4308);
        ALPHA_SEG_CHARS.put("n", 4176);
        ALPHA_SEG_CHARS.put("o", 220);
        ALPHA_SEG_CHARS.put("p", 368);
        ALPHA_SEG_CHARS.put("q", 1158);
        ALPHA_SEG_CHARS.put("r", 80);
        ALPHA_SEG_CHARS.put("s", 8328);
        ALPHA_SEG_CHARS.put("t", 120);
        ALPHA_SEG_CHARS.put("u", 28);
        ALPHA_SEG_CHARS.put("v", 8196);
        ALPHA_SEG_CHARS.put("w", 10260);
        ALPHA_SEG_CHARS.put("x", 10432);
        ALPHA_SEG_CHARS.put("y", 8204);
        ALPHA_SEG_CHARS.put("z", 2120);
        ALPHA_SEG_CHARS.put("{", 2377);
        ALPHA_SEG_CHARS.put("|", 4608);
        ALPHA_SEG_CHARS.put("}", 9353);
        ALPHA_SEG_CHARS.put("~", 1312);
    }

    public AlphaSegment(int address) {
        super(address);
    }

    public void write(String text) throws IOException {
        String[] str = text.split("");
        writeDigitRaw(0, str[0]);
        writeDigitRaw(1, str[1]);
        writeDigitRaw(2, str[2]);
        writeDigitRaw(3, str[3]);
    }

    private void writeDigitRaw(int charNumber, int value) throws IOException {
        if (charNumber > 7)
            return;
        // Set the appropriate digit
        setBufferRow(charNumber, value);
    }


    private void writeDigitRaw(int charNumber, String value) throws IOException {
        if (charNumber > 7)
            return;
        if (value.trim().length() > 1)
            return;
        // Set the appropriate digit
        int byteValue = ALPHA_SEG_CHARS.get(value);
        setBufferRow(charNumber, byteValue);
    }

    /**
     * Turn decimal point on or off at provided position.  Position should be
     *         a value 0 to 3 with 0 being the left most digit on the display.  Decimal
     *         should be True to turn on the decimal point and False to turn it off.
     * @param charNumber (0..3)
     * @param decimal - true or false. whatever you want
     */
    public void set_decimal_point(int charNumber, boolean decimal) throws IOException {
        if (charNumber < 0 || charNumber > 3)
            return;
        // Set bit 14 (decimal point) based on provided value.

        int cell = getBuffer()[charNumber];

        if (decimal)
            setBufferRow(2, cell | 16384); // or
        else
            setBufferRow(2, cell & ~16384); // nand
    }


}
