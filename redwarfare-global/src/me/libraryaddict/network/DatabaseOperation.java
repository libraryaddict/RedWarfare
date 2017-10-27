package me.libraryaddict.network;

import java.sql.Connection;
import java.sql.SQLException;

import me.libraryaddict.mysql.MysqlManager;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.Jedis;

public abstract class DatabaseOperation
{
    private boolean _success;

    protected Connection getMysql() throws SQLException
    {
        return MysqlManager.getConnection();
    }

    protected Jedis getRedis()
    {
        return RedisManager.getRedis();
    }

    public boolean isSuccess()
    {
        return _success;
    }

    protected void setSuccess()
    {
        _success = true;
    }
}
