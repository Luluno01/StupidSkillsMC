package vip.untitled.stupidskills.helpers

import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.*


@Suppress("SqlNoDataSourceInspection")
open class PlayerStore(val context: JavaPlugin, dbName: String, open val table: String) {
    companion object {
        interface Record {
            val uuid: UUID
            val value: String
            val lastUpdate: Date
        }

        enum class Field(val fieldName: String) {
            UUID("uuid"),
            VALUE("value"),
            LAST_UPDATE("last_update");

            override fun toString(): String {
                return fieldName
            }
        }
    }

    open val dbName: String = if (dbName.endsWith(".db")) dbName else "$dbName.db"
    val dbFile = File(context.dataFolder, dbName)
    open val url = "jdbc:sqlite:${if (dbFile.absolutePath.endsWith(".db")) dbName else "${dbFile.absolutePath}.db"}"
    open lateinit var conn: Connection

    open fun init() {
        val dataFolder = context.dataFolder
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        conn = DriverManager.getConnection(url)
        conn.createStatement().use { statement ->
            statement.execute(
                "CREATE TABLE IF NOT EXISTS $table (\n" +
                        "    ${Field.UUID} TEXT PRIMARY KEY,\n" +
                        "    ${Field.VALUE} TEXT,\n" +
                        "    ${Field.LAST_UPDATE} INTEGER NOT NULL\n" +
                        ");"
            )
        }
    }

    open fun set(player: Entity, value: String) {
        conn.prepareStatement("SELECT ${Field.UUID} FROM $table WHERE ${Field.UUID} = ?").use { preparedStatement ->
            preparedStatement.setString(1, player.uniqueId.toString())
            preparedStatement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    // None empty
                    conn.prepareStatement(
                        "UPDATE $table set ${Field.VALUE} = ? ," +
                                "    ${Field.LAST_UPDATE} = ? " +
                                "WHERE ${Field.UUID} = ?"
                    ).use { preparedStatement ->
                        preparedStatement.setString(1, value)
                        preparedStatement.setLong(2, Date().time)
                        preparedStatement.setString(3, player.uniqueId.toString())
                        preparedStatement.executeUpdate()
                    }
                } else {
                    conn.prepareStatement("INSERT INTO $table(${Field.UUID}, ${Field.VALUE}, ${Field.LAST_UPDATE}) VALUES(?, ?, ?)")
                        .use { preparedStatement ->
                            preparedStatement.setString(1, player.uniqueId.toString())
                            preparedStatement.setString(2, value)
                            preparedStatement.setLong(3, Date().time)
                            preparedStatement.executeUpdate()
                        }
                }
            }
        }
    }

    open fun get(player: Entity): Record? {
        var record: Record? = null
        conn.prepareStatement("SELECT ${Field.VALUE}, ${Field.LAST_UPDATE} FROM $table WHERE ${Field.UUID} = ?")
            .use { preparedStatement ->
                preparedStatement.setString(1, player.uniqueId.toString())
                preparedStatement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        record = object : Record {
                            override val uuid = player.uniqueId
                            override val value = resultSet.getString(Field.VALUE.toString())
                            override val lastUpdate: Date = Date(resultSet.getLong(Field.LAST_UPDATE.toString()))
                        }
                    }
                }
            }
        return record
    }

    open fun remove(player: Entity): Record? {
        val record = get(player)
        if (record != null) {
            conn.prepareStatement("DELETE FROM $table WHERE ${Field.UUID} = ?").use { preparedStatement ->
                preparedStatement.setString(1, player.uniqueId.toString())
                preparedStatement.executeUpdate()
            }
        }
        return record
    }

    open fun close() {
        conn.close()
    }
}