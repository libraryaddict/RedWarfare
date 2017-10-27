package me.libraryaddict.mysql;

import java.sql.Connection;
import java.sql.Statement;

import me.libraryaddict.core.utils.UtilError;

public class MysqlCreateTables
{
    public MysqlCreateTables()
    {
        Connection con = null;

        try
        {
            System.out.println("Creating tables..");
            con = MysqlManager.getConnection();

            Statement stmt = con.createStatement();

            stmt.execute(
                    "CREATE TABLE `playerinfo` ( `id` INT NOT NULL AUTO_INCREMENT, `uuid` VARCHAR(36), `type` INT, `info` VARCHAR(45), `first_used` DATETIME, `last_used` DATETIME, PRIMARY KEY (`id`), UNIQUE INDEX `identifier_UNIQUE` (`uuid`, `info` ASC));");
            stmt.execute(
                    "CREATE TABLE `ranks` ( `uuid` VARCHAR(36) NOT NULL, `rank` INT NOT NULL, `expires` DATETIME NULL, `display` BIT(1) NULL, PRIMARY KEY (`uuid`, `rank`));");
            stmt.execute(
                    "CREATE TABLE `currency` ( `uuid` VARCHAR(36) NOT NULL, `type` INT NOT NULL, `amount` BIGINT(20) NOT NULL, PRIMARY KEY (`uuid`, `type`));");
            stmt.execute(
                    "CREATE TABLE `currency_log` ( `id` INT NOT NULL AUTO_INCREMENT, `uuid` VARCHAR(36) NOT NULL, `type` INT NOT NULL, `reason` INT NOT NULL, `amount` BIGINT(20) NULL, `date` DATETIME, PRIMARY KEY (`id`));");
            stmt.execute(
                    "CREATE TABLE `stats` ( `uuid` VARCHAR(36) NOT NULL, `type` INT NOT NULL, `value` BIGINT(20) NULL, PRIMARY KEY (`uuid`, `type`));");
            stmt.execute(
                    "CREATE TABLE `mappings` ( `name` VARCHAR(60) NOT NULL, `value` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`value`), UNIQUE INDEX `name_UNIQUE` (`name` ASC));");
            stmt.execute(
                    "CREATE TABLE `bungee` ( `id` INT NOT NULL AUTO_INCREMENT, `type` VARCHAR(30) NOT NULL, `value` VARCHAR(30000) NOT NULL, PRIMARY KEY (`id`));");
            stmt.execute(
                    "CREATE TABLE `bans` ( `id` INT NOT NULL AUTO_INCREMENT, `banned` VARCHAR(36), `banned_by` INT, `reason` VARCHAR(500), `banned_when` DATETIME, `ban_expires` DATETIME, `display` BIT(1) NULL, `removed` INT, PRIMARY KEY (`id`));");
            stmt.execute(
                    "CREATE TABLE `updates` ( `id` INT NOT NULL AUTO_INCREMENT, `file` VARCHAR(50) NOT NULL, `version` VARCHAR(50) NOT NULL, `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`));");
            stmt.execute(
                    "CREATE TABLE `errors` ( `id` INT NOT NULL AUTO_INCREMENT, `server` VARCHAR(100) NOT NULL, `error` VARCHAR(5000) NOT NULL, `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`));");
            stmt.execute(
                    "CREATE TABLE `prefs_boolean` ( `uuid` VARCHAR(36) NOT NULL, `type` INT NOT NULL, `value` BIT(1) NOT NULL, PRIMARY KEY (`uuid`, `type`));");
            stmt.execute(
                    "CREATE TABLE `prefs_string` ( `uuid` VARCHAR(36) NOT NULL, `type` INT NOT NULL, `value` VARCHAR(100) NOT NULL, PRIMARY KEY (`uuid`, `type`));");
            stmt.execute(
                    "CREATE TABLE `owned` ( `uuid` VARCHAR(36) NOT NULL, `type` INT NOT NULL, PRIMARY KEY (`uuid`, `type`));");
            stmt.execute(
                    "CREATE TABLE `maps` ( `uuid` VARCHAR(36) NOT NULL, `name` VARCHAR(50) NOT NULL, `creator` VARCHAR(36) NOT NULL, `time_created` timestamp, `last_modified` timestamp, `map_type` INT, `desc` VARCHAR(100), `ip_loc` VARCHAR(15), `file_loc` VARCHAR(150), `backup_loc` VARCHAR(10), `backup_version` VARCHAR(45), `deleted` BIT(1) NOT NULL, `backup_freq` INT, `last_backup` timestamp NOT NULL, `visitors` BIT(1) NOT NULL, `releasable` BIT(1) NOT NULL, PRIMARY KEY (`uuid`));");
            stmt.execute(
                    "CREATE TABLE `map_builders` ( `player` VARCHAR(36) NOT NULL, `map` VARCHAR(36) NOT NULL, `rank` INT NOT NULL, PRIMARY KEY (`player`, `map`));");
            stmt.execute(
                    "CREATE TABLE `map_backups` ( `map` VARCHAR(36) NOT NULL, `date` VARCHAR(10) NOT NULL, `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`map`, `date`));");
            stmt.execute(
                    "CREATE TABLE `map_reviews` (`map` VARCHAR(36) NOT NULL, `player` VARCHAR(36) NOT NULL, `rating` INT NOT NULL, `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`map`, `player`));");
            stmt.execute(
                    "CREATE TABLE `command_log` (`id` INT NOT NULL AUTO_INCREMENT, `player` VARCHAR(36) NOT NULL, `command` VARCHAR(36) NOT NULL, `args` VARCHAR(100) NOT NULL, `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`));");
            stmt.execute(
                    "CREATE TABLE `vote_log` ( `id` INT NOT NULL AUTO_INCREMENT, `player` VARCHAR(36) NOT NULL, `site` INT NOT NULL, `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`));");
            stmt.execute(
                    "CREATE TABLE `redeem_codes` ( `id` INT NOT NULL AUTO_INCREMENT, `owner` VARCHAR(36) NOT NULL, `redeemer` VARCHAR(36), `code` VARCHAR(10) NOT NULL, `type` INT NOT NULL, `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, `redeemed` timestamp, PRIMARY KEY (`id`));");
            stmt.execute(
                    "CREATE TABLE `referals` (`referer` VARCHAR(36) NOT NULL, `refered` VARCHAR(36) NOT NULL, `refered_name` VARCHAR(16) NOT NULL, `when` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, `completed` timestamp NULL, PRIMARY KEY (`refered`));");
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
        finally
        {
            try
            {
                if (con != null)
                {
                    con.close();
                }
            }
            catch (Exception ex)
            {
                UtilError.handle(ex);
            }
        }
    }
}
