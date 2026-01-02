package com.group12.greengrocer.database;

import com.group12.greengrocer.models.Coupon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing system-wide settings.
 * <p>
 * This class handles database operations related to discount coupons,
 * customer loyalty program configurations, and general system settings
 * (such as minimum cart value).
 * </p>
 */
public class SettingsDAO {

    // --- COUPON OPERATIONS ---

    /**
     * Retrieves all coupons available in the system.
     *
     * @return A {@link List} of {@link Coupon} objects containing details such as
     *         code, discount percentage, minimum purchase amount, and expiration date.
     */
    public static List<Coupon> getAllCoupons() {
        List<Coupon> list = new ArrayList<>();
        String sql = "SELECT * FROM coupons";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Coupon(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getDouble("discount_percentage"),
                    rs.getDouble("min_purchase_amount"),
                    rs.getTimestamp("valid_until"),
                    rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Adds a new discount coupon to the database.
     * <p>
     * The coupon is set to 'active' by default upon creation.
     * </p>
     *
     * @param code        The unique alphanumeric code for the coupon.
     * @param discount    The percentage of discount (e.g., 10.0 for 10%).
     * @param minPurchase The minimum total cart amount required to use this coupon.
     * @param validUntil  The expiration date of the coupon.
     * @return {@code true} if the coupon was successfully created; {@code false} otherwise.
     */
    public static boolean addCoupon(String code, double discount, double minPurchase, java.time.LocalDate validUntil) {
        String sql = "INSERT INTO coupons (code, discount_percentage, min_purchase_amount, valid_until, is_active) VALUES (?, ?, ?, ?, 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setDouble(2, discount);
            ps.setDouble(3, minPurchase);
            ps.setTimestamp(4, Timestamp.valueOf(validUntil.atStartOfDay()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- LOYALTY SETTINGS ---

    /**
     * Retrieves the current loyalty program configuration.
     * <p>
     * This setting determines how many orders a user must place to become eligible
     * for a loyalty discount.
     * </p>
     *
     * @return An {@code Integer[]} array where:
     *         <ul>
     *           <li>Index 0: Minimum orders required (can be null).</li>
     *           <li>Index 1: Discount percentage.</li>
     *         </ul>
     *         Returns default values {@code {null, 5}} if no settings are found.
     */
    public static Integer[] getLoyaltySettings() {
        String sql = "SELECT min_orders, discount_percentage FROM loyalty_settings WHERE is_active = 1 LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Integer minOrders = rs.getObject("min_orders", Integer.class); // can be null
                double discount = rs.getDouble("discount_percentage");
                return new Integer[]{minOrders, (int)discount};
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new Integer[]{null, 5}; // Default: min_orders null, discount 5%
    }

    /**
     * Updates the loyalty program settings.
     * <p>
     * This method clears existing settings and inserts the new configuration.
     * </p>
     *
     * @param minOrders The number of orders required to qualify for loyalty status.
     * @param discount  The discount percentage awarded to loyal customers.
     */
    public static void updateLoyaltySettings(Integer minOrders, double discount) {
        // Logic: Clear table first, then insert new settings
        String delSql = "DELETE FROM loyalty_settings";
        String insSql = "INSERT INTO loyalty_settings (min_orders, discount_percentage) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement psDel = conn.prepareStatement(delSql)) { psDel.executeUpdate(); }
            try (PreparedStatement psIns = conn.prepareStatement(insSql)) {
                if (minOrders != null) {
                    psIns.setInt(1, minOrders);
                } else {
                    psIns.setNull(1, java.sql.Types.INTEGER);
                }
                psIns.setDouble(2, discount);
                psIns.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- COUPON DELETION ---

    /**
     * Permanently deletes a coupon from the database.
     *
     * @param couponId The unique ID of the coupon to delete.
     * @return {@code true} if the deletion was successful; {@code false} otherwise.
     */
    public static boolean deleteCoupon(int couponId) {
        String sql = "DELETE FROM coupons WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- COUPON UPDATES ---

    /**
     * Updates the details of an existing coupon.
     *
     * @param couponId    The unique ID of the coupon to update.
     * @param code        The new coupon code.
     * @param discount    The new discount percentage.
     * @param minPurchase The new minimum purchase amount.
     * @param validUntil  The new expiration date.
     * @param isActive    The new active status.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     */
    public static boolean updateCoupon(int couponId, String code, double discount, double minPurchase, java.time.LocalDate validUntil, boolean isActive) {
        String sql = "UPDATE coupons SET code = ?, discount_percentage = ?, min_purchase_amount = ?, valid_until = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setDouble(2, discount);
            ps.setDouble(3, minPurchase);
            ps.setTimestamp(4, Timestamp.valueOf(validUntil.atStartOfDay()));
            ps.setBoolean(5, isActive);
            ps.setInt(6, couponId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- MINIMUM CART VALUE ---

    /**
     * Retrieves the configured minimum cart value required for checkout.
     *
     * @return The minimum cart value from the database. Returns {@code 50.0}
     *         if the setting is not found.
     */
    public static double getMinCartValue() {
        String sql = "SELECT value FROM system_settings WHERE setting_key = 'min_cart_value'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 50.0; // Default value
    }

    /**
     * Updates or defines the minimum cart value setting.
     * <p>
     * This method checks if the setting exists; if so, it updates it.
     * If not, it inserts a new record (Upsert logic).
     * </p>
     *
     * @param minValue The new minimum cart value to set.
     * @return {@code true} if the operation was successful; {@code false} otherwise.
     */
    public static boolean updateMinCartValue(double minValue) {
        String checkSql = "SELECT COUNT(*) FROM system_settings WHERE setting_key = 'min_cart_value'";
        String updateSql = "UPDATE system_settings SET value = ? WHERE setting_key = 'min_cart_value'";
        String insertSql = "INSERT INTO system_settings (setting_key, value) VALUES ('min_cart_value', ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql);
                 ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Update existing
                    try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                        psUpdate.setDouble(1, minValue);
                        return psUpdate.executeUpdate() > 0;
                    }
                } else {
                    // Insert new
                    try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                        psInsert.setDouble(1, minValue);
                        return psInsert.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}