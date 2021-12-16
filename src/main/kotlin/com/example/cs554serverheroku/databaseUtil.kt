package com.example.cs554serverheroku

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*


val username = "root"
val password = "rootpass"
val schema = "sdui"
val table = "layouts"

fun getConnection() {
    val connectionProps = Properties()
    connectionProps.put("user", username)
    connectionProps.put("password", password)
    try {
        Class.forName("com.mysql.jdbc.Driver").newInstance()
        conn = DriverManager.getConnection(
            "jdbc:" + "mysql" + "://" +
                    "127.0.0.1" +
                    ":" + "3306" + "/" +
                    "",
            connectionProps)
    } catch (ex: SQLException) {
        // handle any errors
        ex.printStackTrace()
    } catch (ex: Exception) {
        // handle any errors
        ex.printStackTrace()
    }
}

fun initTable(connection: Connection) {
    //SQL statement to create a table
    truncateTable(connection)
    val sql = """
         CREATE TABLE $schema.$table (
            pageID int primary key,
            json varchar(2047)
        """.trimMargin()

    with(connection) {
        //Get and instance of statement from the connection and use
        //the execute() method to execute the sql
        createStatement().execute(sql)

        //Commit the change to the database
        commit()
    }
}

fun truncateTable(connection: Connection) {
    val sql = "TRUNCATE TABLE $schema.$table"
    with (connection) {
        createStatement().execute(sql)
        commit()
    }
}

fun getQuery(connection: Connection, query : String):String {
    val rs = connection.createStatement().executeQuery(query)
    var result = ""
    while (rs.next()) {
        result+=rs.next().toString()
    }
    return result
}

