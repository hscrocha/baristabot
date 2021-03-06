/*
 * MIT License
 *
 * Copyright (c) 2021 Evyn Price
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.evyn.bot.resources;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSource.class);
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    static {

        if (Config.database.equals("MYSQL")) {
            config.setJdbcUrl("jdbc:mysql://" + Config.db_url);
            config.setUsername(Config.db_user);
            config.setPassword(Config.db_pass);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(20);
        } else {
            try {
                final File db = new File("database.db");

                if (!db.exists()) {
                    if (db.createNewFile()) {
                        LOGGER.info("Created database file");
                    } else {
                        LOGGER.info("Could not create database file");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            config.setJdbcUrl("jdbc:sqlite:database.db");
            config.setMaximumPoolSize(20);
            config.setLeakDetectionThreshold(2000);

        }
            ds = new HikariDataSource(config);

        try {
            Connection conn = ds.getConnection();
            conn.isValid(0);
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        // create settings table
        try (final Connection conn = getConnection();
             final Statement statement = conn.createStatement()) {
            final String defaultPrefix = Config.prefix;

            String input = "CREATE TABLE IF NOT EXISTS guild_settings (" +
                    "id INTEGER PRIMARY KEY ";

            if (Config.database.equals("MYSQL")) {
                input += "AUTO_INCREMENT";
            } else {
                input += "AUTOINCREMENT";
            }

            input += "," + "guild_id VARCHAR(20) NOT NULL," +
                    "prefix VARCHAR(255) NOT NULL DEFAULT '" + defaultPrefix +"'," +
                    "embed VARCHAR(1) NOT NULL DEFAULT '1'," + "modlog_id VARCHAR(18) NOT NULL DEFAULT '" +
                    "0" + "'," + "activitylog_id VARCHAR(18) NOT NULL DEFAULT '" +
                    "0'" +
                    ");";

            statement.executeUpdate(input);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // create counting_guilds
        try (final Connection conn = getConnection();
             final Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS counting_guilds (" +
                    "guild_id VARCHAR(20) NOT NULL PRIMARY KEY," + "channel VARCHAR(18) NOT NULL DEFAULT " +
                    "'0'" + ", current_score INTEGER," +
                    "last_userid VARCHAR(18) NOT NULL DEFAULT '0'," + "top_score INTEGER" +
                    ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // create counting_users
       try (final Connection conn = getConnection();
            final Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS counting_users (" +
                    "guild_id VARCHAR(20) NOT NULL," + "user_id VARCHAR(18) NOT NULL," +
                    "total_count INTEGER," + "member_id VARCHAR(38) PRIMARY KEY NOT NULL" +
                    ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
