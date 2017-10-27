package me.libraryaddict.redis.operations;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilFile;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlFetchVersion extends DatabaseOperation
{
    private String _newVersion;

    public MysqlFetchVersion(String fileName)
    {
        try (Connection connection = getMysql())
        {
            PreparedStatement pre = connection.prepareStatement("SELECT version FROM updates WHERE `file` = ?");

            pre.setString(1, fileName);

            ResultSet rs = pre.executeQuery();

            if (rs.first())
            {
                _newVersion = rs.getString("version");
            }

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public String getVersion()
    {
        return _newVersion;
    }

    public boolean isOld(File file)
    {
        return isOld(UtilFile.getSha(file));
    }

    public boolean isOld(String currentVersion)
    {
        return _newVersion != null && !_newVersion.equals(currentVersion);
    }

}
