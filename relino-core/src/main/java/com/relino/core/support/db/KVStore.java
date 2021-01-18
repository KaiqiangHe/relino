package com.relino.core.support.db;

import com.relino.core.support.Utils;
import org.apache.commons.dbutils.handlers.MapHandler;

import java.sql.SQLException;
import java.util.Map;

/**
 * 提供KV存储
 *
 * @author kaiqiang.he
 */
public class KVStore {

    private DBExecutor dbExecutor;

    public KVStore(DBExecutor dbExecutor) {
        Utils.checkNoNull(dbExecutor);
        this.dbExecutor = dbExecutor;
    }

    /**
     * @return 获取key的值，如果key不存在返回null
     */
    public String get(String key) throws SQLException {
        String sql = "select v from kv where k = ?";
        Map<String, Object> row = dbExecutor.query(sql, new MapHandler(), new Object[]{key});

        if(Utils.isEmpty(row)) {
            return null;
        }
        Object v = row.get("v");
        return v == null ? null : (String) v;
    }

    /**
     * 更新key对应的value值，返回更新是否成功
     */
    public boolean update(String key, String newValue) throws SQLException {
        return dbExecutor.execute("update kv set v = ? where k = ?", new Object[] {newValue, key}) == 1;
    }
}
