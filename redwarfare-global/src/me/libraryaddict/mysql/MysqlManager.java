package me.libraryaddict.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;

public class MysqlManager {
    private static ComboPooledDataSource _mysqlPool;

    public static Connection getConnection() throws SQLException {
        assert _mysqlPool != null;

        synchronized (_mysqlPool) {
            System.out.println(
                    "Creating new mysql connection.. " + _mysqlPool.getNumConnections() + " connections active");
            return _mysqlPool.getConnection();
        }
    }

    public static void shutdown() {
        while (true) {
            synchronized (_mysqlPool) {
                try {
                    if (_mysqlPool.getNumBusyConnections() > 0)
                        continue;

                    _mysqlPool.close();
                }
                catch (SQLException e) {
                    UtilError.handle(e);
                }
            }
        }
    }

    public MysqlManager(int idle) {
        System.out.println("Enabling: Mysql Manager");

        try {
            _mysqlPool = new ComboPooledDataSource();
            _mysqlPool.setDriverClass("com.mysql.jdbc.Driver"); // loads the jdbc driver
            _mysqlPool.setJdbcUrl("jdbc:mysql://127.0.0.1/RedWarfare?useSSL=false");
            _mysqlPool.setUser("root");
            _mysqlPool.setPassword("Debian!");
            _mysqlPool.setTestConnectionOnCheckout(true);
            _mysqlPool.setPreferredTestQuery("DO 1");
            _mysqlPool.setMinPoolSize(idle);
            _mysqlPool.setMaxPoolSize(30);

            Connection con = getConnection();

            Statement stmt = con.createStatement();

            try {
                stmt.execute("SELECT 1 FROM bans LIMIT 1;");
                con.close();
            }
            catch (Exception ex) {
                con.close();

                new MysqlCreateTables();
            }
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }

        UtilError.init();

        KeyMappings.loadKeys();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public boolean equals(Object obj) {
                return obj != null && hashCode() == obj.hashCode();
            }

            public int hashCode() {
                return "mysql".hashCode();
            }

            public void run() {
                _mysqlPool.close();
            }
        });
    }
}
