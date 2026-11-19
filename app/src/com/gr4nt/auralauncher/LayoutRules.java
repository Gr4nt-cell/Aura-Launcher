package com.gr4nt.auralauncher;

public final class LayoutRules {
    public static String mode(int widthDp, String choice) {
        if ("phone".equals(choice) || "tablet".equals(choice) || "desktop".equals(choice)) return choice;
        return widthDp >= 1000 ? "desktop" : widthDp >= 600 ? "tablet" : "phone";
    }
    public static int columns(int width, String mode, int preferred) {
        int cap = Math.max(2, width / 74);
        return Math.max(2, Math.min(cap, preferred > 0 ? preferred : "phone".equals(mode) ? 4 : Math.max(6, width / 112)));
    }
    public static int dockCell(int width, int count, int wanted) {
        return Math.max(28, Math.min(wanted, Math.max(28, (width - 48) / Math.max(1, count))));
    }
    public static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private LayoutRules() {}
}
