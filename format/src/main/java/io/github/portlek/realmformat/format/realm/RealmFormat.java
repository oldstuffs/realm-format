package io.github.portlek.realmformat.format.realm;

public interface RealmFormat {
    String EXTENSION = ".realm";

    byte[] HEADER = new byte[] { -79, 11 };

    byte VERSION = 1;

    static byte dataVersionToWorldVersion(final int dataVersion) {
        if (dataVersion <= 0) {
            return (byte) 1;
        } else if (dataVersion < 818) {
            return (byte) 2;
        } else if (dataVersion < 1501) {
            return (byte) 3;
        } else if (dataVersion < 1517) {
            return (byte) 4;
        } else if (dataVersion < 2566) {
            return (byte) 5;
        } else if (dataVersion <= 2586) {
            return (byte) 6;
        } else if (dataVersion <= 2730) {
            return (byte) 7;
        } else if (dataVersion <= 3218) {
            return (byte) 8;
        }
        throw new UnsupportedOperationException("Unsupported world version: " + dataVersion);
    }
}
