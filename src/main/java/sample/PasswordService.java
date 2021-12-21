package sample;


import org.json.JSONArray;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.*;

import static sample.DBConstants.*;
import static sample.UserQueries.SELECT_ALL_USERS;
import static sample.UserQueries.UPDATE_USER_QUERY;


public class PasswordService {
    private Map<String, KeyEntity> clampedKeys;
    private Map<OverlayType, Integer> overlays;
    private KeyEntity prevKeyEntity;
    private boolean isOverlay;
    private int AMPLITUDE = 2;
    private static double maxErr = 0.2;
    private String userNotFound = "Пользователь не найден";

    public boolean isOverlay() {
        return isOverlay;
    }

    public void setOverlay(boolean overlay) {
        isOverlay = overlay;
    }

    public PasswordService() {
        clampedKeys = new HashMap<>();
        overlays = new HashMap<>();
        overlays.put(OverlayType.FIRST_TYPE, 0);
        overlays.put(OverlayType.SECOND_TYPE, 0);
        isOverlay = false;
    }

    public int clampedKeysSize() {
        return clampedKeys.size();
    }

    public void addClampedKey(String key, KeyEntity keyEntity) {
        clampedKeys.put(key, keyEntity);
    }

    public void clearClampedKeys(String key) {
        prevKeyEntity = clampedKeys.get(key);
        clampedKeys.remove(key);
    }

    public void checkOverlay(String key) {
        KeyEntity currentKeyEntity = clampedKeys.get(key);
        if (currentKeyEntity.getUpTime() > prevKeyEntity.getUpTime() && currentKeyEntity.getDownTime() > prevKeyEntity.getDownTime()) {
            overlays.put(OverlayType.FIRST_TYPE, overlays.get(OverlayType.FIRST_TYPE) + 1);
        }
        if (currentKeyEntity.getDownTime() < prevKeyEntity.getDownTime() && prevKeyEntity.getUpTime() < currentKeyEntity.getUpTime()) {
            overlays.put(OverlayType.SECOND_TYPE, overlays.get(OverlayType.SECOND_TYPE) + 1);
        }
        isOverlay = false;
    }

    public KeyEntity getKeyEntity(String key) {
        return clampedKeys.get(key);
    }

    public String getOverlayStatistic() {
        return "1 вид " + overlays.get(OverlayType.FIRST_TYPE) + " \n2 вид " + overlays.get(OverlayType.SECOND_TYPE);
    }

    public int calculate(long t, List<KeyEntity> list) {
        int result = 0;
        for (KeyEntity k : list) {
            if (k.getDownTime() <= t && t <= k.getUpTime()) {
                result += AMPLITUDE;
            }
        }
        return result;
    }

    public double calculateHaar(int r, int m, long t) {
        double tDouble = t / 1000.0;
        double result = 0;
        if (tDouble >= 1.0) {
            tDouble = 0.999;
        }
        if (((m - 1) / Math.pow(2, r)) <= tDouble && tDouble < ((m - 0.5) / Math.pow(2, r))) {
            result = Math.pow(2, r / 2.0);
        } else if (((m - 0.5) / Math.pow(2, r)) <= tDouble && tDouble <= (m / Math.pow(2, r))) {
            result = -Math.pow(2, r / 2.0);
        }

        return result;

    }

    public double[] calculateVector(List<KeyEntity> list) {
        double[] vector = new double[list.size()];
        double result;
        double haar;
        double countVariable = 1.0 / list.size();
        double sum = 0;
        for (int i = 0; i < list.size(); i++) {
            for (int k = 0; k < list.size(); k++) {
                result = calculate(list.get(k).getDownTime(), list);
                haar = calculateHaar(k, i, list.get(k).getUpTime() - list.get(k).getDownTime());
                sum += result * haar;
            }
            vector[i] = sum * countVariable;
            sum = 0;

        }
        return vector;
    }

    public String userIdentification(double[] vector) {
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement stmt = conn.createStatement()) {
            ResultSet set = stmt.executeQuery(SELECT_ALL_USERS);
            while (set.next()) {
                try {
                    JSONArray dbVector = new JSONArray(set.getArray("vector").toString());
                    if (compareVector(dbVector, vector)) {
                        return set.getString("username");
                    }
                } catch (Exception e) {
                }
            }

        } catch (SQLException throwables) {
        }
        return userNotFound;
    }

    public static boolean compareVector(JSONArray v1, double[] v2) {
        if (v1.length() != v2.length) {
            return false;
        }
        for (int i = 0; i < v2.length; i++) {
            if (Math.abs(v2[i]) - Math.abs(v1.getDouble(i)) > maxErr) {
                return false;
            }
        }
        return true;
    }

    private enum OverlayType {
        FIRST_TYPE,
        SECOND_TYPE
    }
}
