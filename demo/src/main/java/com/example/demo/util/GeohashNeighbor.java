package com.example.demo.util;

import java.util.HashMap;
import java.util.Map;

public class GeohashNeighbor {

    // 1. Định nghĩa các hướng
    public enum Direction { TOP, BOTTOM, RIGHT, LEFT }

    // 2. Mapping data
    private static final Map<Direction, String[]> NEIGHBORS = new HashMap<>();
    private static final Map<Direction, String[]> BORDERS = new HashMap<>();

    static {
        // Thứ tự parity: [0] = Even, [1] = Odd
        // Base32 map chuẩn: 0123456789bcdefghjkmnpqrstuvwxyz

        // NEIGHBORS MAP
        NEIGHBORS.put(Direction.RIGHT,  new String[] { "bc01fg45238967deuvhjyznpkmstqrwx", "p0r21436x8zb9dcf5h7kjnmqesgutwvy" });
        NEIGHBORS.put(Direction.LEFT,   new String[] { "238967debc01fg45kmstqrwxuvhjyznp", "14365h7k9dcfesgujnmqp0r2twvyx8zb" });
        NEIGHBORS.put(Direction.TOP,    new String[] { "p0r21436x8zb9dcf5h7kjnmqesgutwvy", "bc01fg45238967deuvhjyznpkmstqrwx" });
        NEIGHBORS.put(Direction.BOTTOM, new String[] { "14365h7k9dcfesgujnmqp0r2twvyx8zb", "238967debc01fg45kmstqrwxuvhjyznp" });

        // BORDERS MAP
        BORDERS.put(Direction.RIGHT,  new String[] { "bcfguvyz", "prxz" });
        BORDERS.put(Direction.LEFT,   new String[] { "0145hjnp", "028b" });
        BORDERS.put(Direction.TOP,    new String[] { "prxz", "bcfguvyz" });
        BORDERS.put(Direction.BOTTOM, new String[] { "028b", "0145hjnp" });
    }

    // Hàm tìm neighbor
    public static String calculate(String srcHash, Direction dir) {
        if (srcHash == null || srcHash.isEmpty()) return "";

        srcHash = srcHash.toLowerCase();
        char lastChar = srcHash.charAt(srcHash.length() - 1);
        String parent = srcHash.substring(0, srcHash.length() - 1);

        // Parity: Even = 0, Odd = 1 based on LENGTH
        int type = srcHash.length() % 2;

        // Check Border
        String borderStr = BORDERS.get(dir)[type];

        // Nếu lastChar nằm trong border string, ta phải xử lý parent trước
        if (borderStr.indexOf(lastChar) != -1 && !parent.isEmpty()) {
            parent = calculate(parent, dir);
        }

        // Tìm mapping cho lastChar
        String neighborStr = NEIGHBORS.get(dir)[type];

        // Vị trí của lastChar trong chuỗi gốc Base32
        // Base32 chuẩn: 0123456789bcdefghjkmnpqrstuvwxyz
        String base32 = "0123456789bcdefghjkmnpqrstuvwxyz";
        int index = base32.indexOf(lastChar);

        // Lấy ký tự tương ứng từ chuỗi mapping
        char newLastChar = neighborStr.charAt(index);

        return parent + newLastChar;
    }


}
