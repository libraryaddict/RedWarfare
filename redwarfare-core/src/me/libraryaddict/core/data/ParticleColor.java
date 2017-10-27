package me.libraryaddict.core.data;

public enum ParticleColor {
    AQUA(-1, 1, 1), BLUE(-1, 0, 1), DARK_PURPLE(-1, 0, -1), DARK_RED(-1, 0, 0), GOLD(0, 1, 0), GREEN(-1, 1, 0), ORANGE(1, -1,
            0), PINK(0, -1, 1), PURPLE(1, 0, 1), RED(0, 0, 0), SOOT(-1, -1, -1), WHITE(0, 1, 1), YELLOW(1, 1, 0);

    private int[] _colors;

    private ParticleColor(int x, int y, int z) {
        _colors = new int[] {
                x, y, z
        };
    }

    public int[] getColors() {
        return _colors;
    }

    public int getX() {
        return getColors()[0];
    }

    public int getY() {
        return getColors()[1];
    }

    public int getZ() {
        return getColors()[2];
    }
}
