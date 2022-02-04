package com.abtasty.flagship.database;

import com.abtasty.flagship.cache.CacheManager;
import com.abtasty.flagship.cache.IHitCacheImplementation;
import com.abtasty.flagship.cache.IVisitorCacheImplementation;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

/**
 * This class provide a default CacheManager implementation based on local SQLITE database connector.
 * Caution: It is recommended to use it only in a client environment meaning managing only one visitor at a time,
 * as it won't be efficient enough on server environment with multiple visitors at the same time.
 */
public class SQLiteCacheManager extends CacheManager {

    private static final String databaseName = "flagship_database.db";
    private String databasePath = "./";
    private String url = "jdbc:sqlite:" + databasePath + databaseName;

    String _CREATE_VISITORS_TABLE_ = "CREATE TABLE IF NOT EXISTS visitors (\n"
            + "	visitorId VARCHAR(100) UNIQUE PRIMARY KEY,\n"
            + "	content text NOT NULL\n"
            + ");";

    String _CREATE_HITS_TABLE_ = "CREATE TABLE IF NOT EXISTS hits (\n"
            + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
            + "	visitorId VARCHAR(100),\n"
            + "	content text NOT NULL\n"
            + ");";

    /**
     * This class provide a default CacheManager implementation based on local SQLITE database connector.
     * Caution: It is recommended to use it only in a client environment meaning managing only one visitor at a time,
     * as it won't be efficient enough on server environment with multiple visitors at the same time.
     */
    public SQLiteCacheManager() {
        createDatabase();
    }

    /**
     * This class provide a default CacheManager implementation based on local SQLITE database connector.
     * Caution: It is recommended to use it only in a client environment meaning managing only one visitor at a time,
     * as it won't be efficient enough on server environment with multiple visitors at the same time.
     *
     * @param path Path to the database folder destination.
     */
    public SQLiteCacheManager(String path) {
        try {
            this.databasePath = path + (!path.endsWith("/") ? "/" : "");
            Files.createDirectories(Paths.get(databasePath));
            url = "jdbc:sqlite:" + databasePath + databaseName;
        } catch (Exception e) {
            FlagshipLogManager.exception(e);
        }
        createDatabase();
    }



    synchronized private Connection connect() {
        try {
            return DriverManager.getConnection(url);
        } catch (Exception e) {
            FlagshipLogManager.exception(e);
            return null;
        }
    }

    synchronized private void createDatabase() {
        try (Connection conn = this.connect()) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                FlagshipLogManager.log(FlagshipLogManager.Tag.CACHE, LogManager.Level.INFO, String.format(FlagshipConstants.Info.SQLITE_DATABASE_CREATION, meta.getURL()));
                Statement statement = conn.createStatement();
                statement.execute(_CREATE_VISITORS_TABLE_);
                statement.execute(_CREATE_HITS_TABLE_);
            }
        } catch (SQLException e) {
            FlagshipLogManager.exception(e);
        }
    }

    synchronized private int upsertVisitor(String visitorId, JSONObject data) {

        int results = 0;
        boolean exists = isVisitorExists(visitorId);
        String sql = (!exists) ? "INSERT INTO visitors(visitorId,content) VALUES(?,?)" : "UPDATE visitors SET content = ? WHERE visitorId = ?";
        try (Connection conn = connect()) {
            if (conn != null) {
                PreparedStatement statement = conn.prepareStatement(sql);
                if (!exists) {
                    statement.setString(1, visitorId);
                    statement.setString(2, data.toString());
                } else {
                    statement.setString(1, data.toString());
                    statement.setString(2, visitorId);
                }
                results = statement.executeUpdate();
                statement.close();
            }
        } catch (Exception e) {
            FlagshipLogManager.exception(e);
        }
        return results;
    }

    synchronized private boolean isVisitorExists(String visitorId) {
        boolean result = false; //It must returns womething != than getVisitor if the result is badly formatted
        String sql = "SELECT visitorId, content FROM visitors WHERE visitorId = ?";
        try (Connection conn = connect()) {
            if (conn != null) {
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, visitorId);
                ResultSet rs = statement.executeQuery();
                result = rs.next();
                rs.close();
                statement.close();
            }
        }
        catch (Exception e) {
            FlagshipLogManager.exception(e);
        }
        return result;
    }

    synchronized private JSONObject getVisitor(String visitorId) {
        JSONObject result = new JSONObject();
        String sql = "SELECT visitorId, content FROM visitors WHERE visitorId = ?";
        try (Connection conn = connect()) {
            if (conn != null) {
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, visitorId);
                ResultSet rs = statement.executeQuery();
                if (rs.next())
                    result = new JSONObject(rs.getString("content"));
                rs.close();
                statement.close();
            }
        }
        catch (Exception e) {
            FlagshipLogManager.exception(e);
        }
        return result;
    }

    synchronized private int deleteVisitor(String visitorId) {
        int result = 0;
        String sql = "DELETE FROM visitors WHERE visitorId = ?";
        try (Connection conn = connect()) {
            if (conn != null) {
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, visitorId);
                result = statement.executeUpdate();
                statement.close();
            }
        } catch (SQLException e) {
            FlagshipLogManager.exception(e);
        }
        return result;
    }

    synchronized private int insertHit(String visitorId, JSONObject data) {
        int result = 0;
        String sql = "INSERT INTO hits(visitorId,content) VALUES(?,?)";
        try (Connection conn = connect()) {
            if (conn != null) {
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, visitorId);
                statement.setString(2, data.toString());
                result = statement.executeUpdate();
                statement.close();
            }
        } catch (Exception e) {
            FlagshipLogManager.exception(e);
        }
        return result;
    }

    synchronized private JSONArray getHits(String visitorId) {
        String sql = "SELECT * FROM hits WHERE visitorId = ?";
        ArrayList<Integer> hitsToDelete = new ArrayList<>();
        JSONArray results = new JSONArray();
        try (Connection conn = connect()) {
            if (conn != null) {
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, visitorId);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    try {
                        results.put(new JSONObject(rs.getString("content")));
                    } catch (Exception e) {
                        FlagshipLogManager.exception(e);
                    }
                    hitsToDelete.add(rs.getInt("id"));
                }
                rs.close();
                statement.close();
            }
        } catch (Exception e) {
            FlagshipLogManager.exception(e);
        }
        if (!hitsToDelete.isEmpty())
            deleteHits(visitorId, hitsToDelete);
        return results;
    }

    synchronized private int deleteHits(String visitorId) {
        int results = 0;
        String sql = "DELETE FROM hits WHERE visitorId = ?";
        try (Connection conn = connect()) {
            if (conn != null) {
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, visitorId);
                results = statement.executeUpdate();
                statement.close();
            }
        } catch (SQLException e) {
            FlagshipLogManager.exception(e);
        }
        return results;
    }

    synchronized private void deleteHits(String visitorId, ArrayList<Integer> hitsId) {
        for (Integer hitId : hitsId) {
            deleteHit(visitorId, hitId);
        }
    }

    synchronized private int deleteHit(String visitorId, int hitId) {
        int results = 0;
        String sql = "DELETE FROM hits WHERE visitorId = ? AND id = ?";
        try (Connection conn = connect()) {
            if (conn != null) {
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, visitorId);
                statement.setInt(2, hitId);
                results = statement.executeUpdate();
                statement.close();
            }
        } catch (SQLException e) {
            FlagshipLogManager.exception(e);
        }
        return results;
    }


    IVisitorCacheImplementation cacheVisitorImplementation = new IVisitorCacheImplementation() {

        @Override
        public void cacheVisitor(String visitorId, JSONObject data) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.SQLITE_CACHE_MANAGER, LogManager.Level.INFO,
                    String.format(FlagshipConstants.Info.SQLITE_CACHE_MANAGER_CACHE_VISITOR, visitorId, data.toString(4)));
            upsertVisitor(visitorId, data);
        }

        @Override
        public JSONObject lookupVisitor(String visitorId) {
            JSONObject result = getVisitor(visitorId);
            FlagshipLogManager.log(FlagshipLogManager.Tag.SQLITE_CACHE_MANAGER, LogManager.Level.INFO,
                    String.format(FlagshipConstants.Info.SQLITE_CACHE_MANAGER_LOOKUP_VISITOR, visitorId, result.toString(4)));
            return result;
        }

        @Override
        public void flushVisitor(String visitorId) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.SQLITE_CACHE_MANAGER, LogManager.Level.INFO,
                    String.format(FlagshipConstants.Info.SQLITE_CACHE_MANAGER_FLUSH_VISITOR, visitorId));
            deleteVisitor(visitorId);
        }
    };

    IHitCacheImplementation hitCacheImplementation = new IHitCacheImplementation() {
        @Override
        public void cacheHit(String visitorId, JSONObject data) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.SQLITE_CACHE_MANAGER, LogManager.Level.INFO,
                    String.format(FlagshipConstants.Info.SQLITE_CACHE_MANAGER_CACHE_HIT, visitorId, data.toString(4)));
            insertHit(visitorId, data);
        }

        @Override
        public JSONArray lookupHits(String visitorId) {
            JSONArray result = getHits(visitorId);
            FlagshipLogManager.log(FlagshipLogManager.Tag.SQLITE_CACHE_MANAGER, LogManager.Level.INFO,
                    String.format(FlagshipConstants.Info.SQLITE_CACHE_MANAGER_LOOKUP_HIT, visitorId, result.toString(4)));
            return result;
        }

        @Override
        public void flushHits(String visitorId) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.SQLITE_CACHE_MANAGER, LogManager.Level.INFO,
                    String.format(FlagshipConstants.Info.SQLITE_CACHE_MANAGER_FLUSH_HIT, visitorId));
            deleteHits(visitorId);
        }
    };

    @Override
    public IVisitorCacheImplementation getVisitorCacheImplementation() {
        return cacheVisitorImplementation;
    }

    @Override
    public IHitCacheImplementation getHitCacheImplementation() {
        return hitCacheImplementation;
    }
}
