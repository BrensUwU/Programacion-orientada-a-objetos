import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/herenciasimple";
        String user = "root";
        String password = "root";

        try (Connection conn = DriverManager.getConnection(url, user, password)){
            //Insertar en persona
            String sqlPersona = "INSERT INTO Persona (id_persona, nombre, edad) VALUES (?, ?, ?)";
            try (PreparedStatement pstmtPersona = conn.prepareStatement(sqlPersona)) {
            pstmtPersona.setInt(1, 3);
            pstmtPersona.setString(2, "Juan");
            pstmtPersona.setInt(3, 30);
            pstmtPersona.executeUpdate();
            }
            String sqlEmpleado = "Insert INTO Empleado (id_empleado, salario) VALUES (?, ?)";
            try (PreparedStatement pstmtEmpleado = conn.prepareStatement(sqlEmpleado)){
                pstmtEmpleado.setInt(1, 3);
                pstmtEmpleado.setDouble(2, 30000.0);
                pstmtEmpleado.executeUpdate();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}