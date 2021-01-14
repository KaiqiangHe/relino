package com.relino.core.support.db;

import com.relino.core.support.Utils;
import org.apache.commons.dbutils.handlers.MapHandler;

import java.util.Map;

/**
 * @author kaiqiang.he
 */
public class DBPessimisticLock {

    private DBExecutor dbExecutor;

    public DBPessimisticLock(DBExecutor dbExecutor) {
        Utils.checkNoNull(dbExecutor);
        this.dbExecutor = dbExecutor;
    }

    /**
     * 开启数据库悲观锁
     *
     * @throws Exception 加分布式锁失败
     */
    public void openPessimisticLock(String lockKey) throws Exception{
        Utils.checkNonEmpty(lockKey);

        if(!dbExecutor.isInTx()) {
            throw new RuntimeException("数据库悲观锁必须在事务中, lockKey = " + lockKey);
        }

        Map<String, Object> rows = dbExecutor.query(
                "select lock_key from pessimistic_lock where lock_key = ? for update",
                new MapHandler(),
                new Object[]{lockKey});

        if(Utils.isEmpty(rows)) {
            throw new RuntimeException("添加悲观锁失败, lockKey = " + lockKey + "不存在 ");
        }
    }
}
