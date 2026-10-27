package net.mysterria.titles.gui;

public class Layout {

    public static final int ROWS = 6;
    public static final int LOW = 9 * ROWS;

    public static final int[] COLLECTION = block(LOW, 3, 2);
    public static final int[] EQUIPPED = block(LOW + 3, 3, 2);
    public static final int EQUIPPED_PEDESTAL = LOW + 4;
    public static final int[] FILTER = block(LOW + 6, 3, 2);
    public static final int[] PREVIOUS = block(LOW + 18, 3, 2);
    public static final int[] CLOSE = block(LOW + 21, 3, 2);
    public static final int[] NEXT = block(LOW + 24, 3, 2);

    public static int[] block(int topLeft, int cols, int rows) {
        int[] slots = new int[cols * rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                slots[r * cols + c] = topLeft + c + 9 * r;
            }
        }
        return slots;
    }
}
