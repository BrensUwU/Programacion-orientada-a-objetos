package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    // URL de la base de datos y credenciales de acceso
    private static final String URL = "jdbc:mysql://localhost:3306/Restaurante"; // Base de datos 'Restaurante'
    private static final String USER = "root"; // Usuario MySQL
    private static final String PASSWORD = "root"; // Contraseña MySQL

    // Método para establecer la conexión a la base de datos
    public Connection conectar() {
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa a la base de datos");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al conectar a la base de datos");
        }
        return conexion;
    }
}
