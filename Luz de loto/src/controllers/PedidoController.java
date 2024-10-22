package controllers;

import models.Pedido;
import models.Platillo;
import models.Conexion;

import java.sql.*;
import java.util.*;

public class PedidoController {
    private List<Platillo> menu;
    private List<Pedido> pedidos;
    private Conexion conexion;

    // Constructor
    public PedidoController() {
        menu = new ArrayList<>();
        pedidos = new ArrayList<>();
        conexion = new Conexion();  // Instancia de la conexión a la base de datos
        cargarMenu(); // Método para inicializar el menú
    }

    // Método para cargar el menú desde la base de datos
    private void cargarMenu() {
        Connection conn = conexion.conectar(); // Conexión con la base de datos
        String sql = "SELECT id_platillo, nombre_platillo, precio FROM Platillo"; // Consulta SQL para obtener el menú

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery(); // Ejecuta la consulta y guarda el resultado
            while (rs.next()) {
                int id = rs.getInt("id_platillo"); // Obtener el ID del platillo
                String nombre = rs.getString("nombre_platillo"); // Obtener el nombre del platillo
                double precio = rs.getDouble("precio"); // Obtener el precio
                menu.add(new Platillo(id, nombre, precio)); // Añadir el platillo al menú
            }
            System.out.println("Menú cargado desde la base de datos.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para registrar un nuevo pedido
    public void registrarPedido(String nombreCliente) {
        Scanner scanner = new Scanner(System.in);
        Map<Platillo, Integer> platillosConCantidad = new HashMap<>(); // Guardar platillos y sus cantidades
        double total = 0;
        String opcion;

        System.out.println("Menú de platillos:");
        // Mostrar menú solo una vez
        for (int i = 0; i < menu.size(); i++) {
            Platillo platillo = menu.get(i);
            System.out.println((i + 1) + ". " + platillo.getNombre() + " - $" + platillo.getPrecio());
        }

        do {
            System.out.println("Seleccione el platillo o 'S' para finalizar:");
            opcion = scanner.nextLine();

            if (!opcion.equalsIgnoreCase("S")) {
                Platillo platilloSeleccionado = null;

                try {
                    int indice = Integer.parseInt(opcion) - 1; // Conversión de opción a número
                    if (indice >= 0 && indice < menu.size()) {
                        platilloSeleccionado = menu.get(indice); // Selecciona por número
                    }
                } catch (NumberFormatException e) {
                    // Selección por nombre
                    for (Platillo platillo : menu) {
                        if (platillo.getNombre().equalsIgnoreCase(opcion)) {
                            platilloSeleccionado = platillo;
                            break;
                        }
                    }
                }

                if (platilloSeleccionado != null) {
                    System.out.println("Cantidad de " + platilloSeleccionado.getNombre() + ":");
                    int cantidad = Integer.parseInt(scanner.nextLine());

                    platillosConCantidad.put(platilloSeleccionado, cantidad); // Añade platillo y cantidad
                    total += platilloSeleccionado.getPrecio() * cantidad; // Calcula el total
                    System.out.println(cantidad + " x " + platilloSeleccionado.getNombre() + " añadidos.");
                } else {
                    System.out.println("Selección inválida.");
                }
            }

        } while (!opcion.equalsIgnoreCase("S"));

        guardarPedidoEnBaseDeDatos(nombreCliente, platillosConCantidad, total); // Guarda el pedido
    }

    // Guardar el pedido en la base de datos
    private void guardarPedidoEnBaseDeDatos(String nombreCliente, Map<Platillo, Integer> platillosConCantidad, double total) {
        Connection conn = conexion.conectar();

        try {
            conn.setAutoCommit(false); // Inicia la transacción

            // Inserta el cliente
            String sqlCliente = "INSERT INTO Cliente (nombre_cliente) VALUES (?)";
            PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS);
            stmtCliente.setString(1, nombreCliente);
            stmtCliente.executeUpdate();
            ResultSet rsCliente = stmtCliente.getGeneratedKeys();
            int idCliente = rsCliente.next() ? rsCliente.getInt(1) : 0;

            // Inserta el pedido
            String sqlPedido = "INSERT INTO Pedido (id_cliente, total) VALUES (?, ?)";
            PreparedStatement stmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);
            stmtPedido.setInt(1, idCliente);
            stmtPedido.setDouble(2, total);
            stmtPedido.executeUpdate();
            ResultSet rsPedido = stmtPedido.getGeneratedKeys();
            int idPedido = rsPedido.next() ? rsPedido.getInt(1) : 0;

            // Inserta los detalles del pedido
            String sqlDetalle = "INSERT INTO DetallePedido (id_pedido, id_platillo, cantidad) VALUES (?, ?, ?)";
            PreparedStatement stmtDetalle = conn.prepareStatement(sqlDetalle);

            for (Map.Entry<Platillo, Integer> entry : platillosConCantidad.entrySet()) {
                Platillo platillo = entry.getKey();
                int cantidad = entry.getValue();

                stmtDetalle.setInt(1, idPedido);
                stmtDetalle.setInt(2, platillo.getId());
                stmtDetalle.setInt(3, cantidad);
                stmtDetalle.executeUpdate(); // Ejecuta el insert por cada platillo
            }

            conn.commit(); // Confirma la transacción
            System.out.println("Pedido guardado correctamente.");
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback(); // Reversión en caso de error
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Restaurar a su estado normal
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para listar pedidos y mostrarlos en formato tabla
    public void listarPedidos() {
        Connection conn = conexion.conectar();
        String sql = "SELECT Pedido.id_pedido, Cliente.nombre_cliente, Pedido.total, Pedido.fecha, " +
                "Platillo.nombre_platillo, DetallePedido.cantidad " +
                "FROM Pedido " +
                "JOIN Cliente ON Pedido.id_cliente = Cliente.id_cliente " +
                "JOIN DetallePedido ON Pedido.id_pedido = DetallePedido.id_pedido " +
                "JOIN Platillo ON DetallePedido.id_platillo = Platillo.id_platillo " +
                "ORDER BY Pedido.id_pedido";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            // Formato de tabla
            System.out.printf("%-10s %-20s %-10s %-20s %-20s %-10s\n", "ID Pedido", "Cliente", "Total", "Fecha", "Platillo", "Cantidad");

            int lastPedidoId = -1;
            while (rs.next()) {
                int idPedido = rs.getInt("id_pedido");
                String nombreCliente = rs.getString("nombre_cliente");
                double total = rs.getDouble("total");
                Timestamp fecha = rs.getTimestamp("fecha");
                String nombrePlatillo = rs.getString("nombre_platillo");
                int cantidad = rs.getInt("cantidad");

                // Mostrar datos en formato tabla
                if (idPedido != lastPedidoId) {
                    System.out.printf("%-10d %-20s %-10.2f %-20s\n", idPedido, nombreCliente, total, fecha);
                    lastPedidoId = idPedido;
                }
                System.out.printf("%-20s %-10d\n", nombrePlatillo, cantidad);
            }

            if (lastPedidoId == -1) {
                System.out.println("No hay pedidos registrados.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // Método para actualizar un pedido existente
    public void actualizarPedido(String nombreCliente) {
        Connection conn = conexion.conectar();
        Scanner scanner = new Scanner(System.in);

        // Buscar cliente
        String sqlCliente = "SELECT id_cliente FROM Cliente WHERE nombre_cliente = ?";
        String sqlPedido = "SELECT id_pedido, total FROM Pedido WHERE id_cliente = ?";
        String sqlActualizarDetalle = "UPDATE DetallePedido SET cantidad = ? WHERE id_pedido = ? AND id_platillo = ?";
        String sqlRecalcularTotal = "UPDATE Pedido SET total = ? WHERE id_pedido = ?";

        try (PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente)) {
            stmtCliente.setString(1, nombreCliente);
            ResultSet rsCliente = stmtCliente.executeQuery();

            if (rsCliente.next()) {
                int idCliente = rsCliente.getInt("id_cliente");

                // Mostrar pedidos del cliente
                try (PreparedStatement stmtPedido = conn.prepareStatement(sqlPedido)) {
                    stmtPedido.setInt(1, idCliente);
                    ResultSet rsPedidos = stmtPedido.executeQuery();

                    if (!rsPedidos.isBeforeFirst()) {
                        System.out.println("No se encontraron pedidos para este cliente.");
                        return;
                    }

                    System.out.println("Pedidos de " + nombreCliente + ":");
                    while (rsPedidos.next()) {
                        int idPedido = rsPedidos.getInt("id_pedido");
                        double total = rsPedidos.getDouble("total");
                        System.out.println("ID Pedido: " + idPedido + " - Total: $" + total);
                    }

                    // Solicitar ID del pedido a actualizar
                    System.out.println("Ingrese el ID del pedido que desea actualizar: ");
                    int idPedidoActualizar = scanner.nextInt();
                    scanner.nextLine();  // Limpiar buffer

                    // Mostrar platillos disponibles
                    System.out.println("Menú de platillos:");
                    for (int i = 0; i < menu.size(); i++) {
                        Platillo platillo = menu.get(i);
                        System.out.println((i + 1) + ". " + platillo.getNombre() + " - $" + platillo.getPrecio());
                    }

                    // Solicitar platillo y cantidad a actualizar
                    System.out.println("Ingrese el número del platillo que desea actualizar: ");
                    int indicePlatillo = scanner.nextInt() - 1;

                    System.out.println("Ingrese la nueva cantidad: ");
                    int nuevaCantidad = scanner.nextInt();

                    Platillo platilloSeleccionado = menu.get(indicePlatillo);

                    // Actualizar la cantidad del platillo en el pedido
                    try (PreparedStatement stmtActualizarDetalle = conn.prepareStatement(sqlActualizarDetalle)) {
                        stmtActualizarDetalle.setInt(1, nuevaCantidad);
                        stmtActualizarDetalle.setInt(2, idPedidoActualizar);
                        stmtActualizarDetalle.setInt(3, platilloSeleccionado.getId());

                        int filasAfectadas = stmtActualizarDetalle.executeUpdate();
                        if (filasAfectadas > 0) {
                            System.out.println("Pedido actualizado correctamente.");

                            // Recalcular el total del pedido
                            double nuevoTotal = recalcularTotalPedido(idPedidoActualizar, conn);
                            // Actualizar el total en la base de datos
                            try (PreparedStatement stmtActualizarTotal = conn.prepareStatement(sqlRecalcularTotal)) {
                                stmtActualizarTotal.setDouble(1, nuevoTotal);
                                stmtActualizarTotal.setInt(2, idPedidoActualizar);
                                stmtActualizarTotal.executeUpdate();
                                System.out.println("Total del pedido actualizado a: $" + nuevoTotal);
                            }
                        } else {
                            System.out.println("No se encontró ese platillo en el pedido.");
                        }
                    }

                }
            } else {
                System.out.println("Cliente no encontrado.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para recalcular el total del pedido
    private double recalcularTotalPedido(int idPedido, Connection conn) {
        double total = 0.0;
        // Asegúrate de que la columna "id_platillo" y "precio" existan en DetallePedido y Platillo respectivamente
        String sqlDetalles = "SELECT d.cantidad, p.precio FROM DetallePedido d " +
                "INNER JOIN Platillo p ON d.id_platillo = p.id_platillo WHERE d.id_pedido = ?";

        try (PreparedStatement stmtDetalles = conn.prepareStatement(sqlDetalles)) {
            stmtDetalles.setInt(1, idPedido);
            ResultSet rsDetalles = stmtDetalles.executeQuery();

            while (rsDetalles.next()) {
                int cantidad = rsDetalles.getInt("cantidad");
                double precio = rsDetalles.getDouble("precio");
                total += cantidad * precio;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return total;
    }


    // Método para eliminar un pedido
    public void eliminarPedido(String nombreCliente) {
        Connection conn = conexion.conectar();
        Scanner scanner = new Scanner(System.in);

        // Buscar el cliente por nombre
        String sqlCliente = "SELECT id_cliente FROM Cliente WHERE nombre_cliente = ?";
        String sqlEliminarDetalles = "DELETE FROM DetallePedido WHERE id_pedido = ?";
        String sqlEliminarPedido = "DELETE FROM Pedido WHERE id_cliente = ? AND id_pedido = ?";

        try (PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente)) {
            stmtCliente.setString(1, nombreCliente);
            ResultSet rsCliente = stmtCliente.executeQuery();

            if (rsCliente.next()) {
                int idCliente = rsCliente.getInt("id_cliente");

                // Mostrar los pedidos de ese cliente
                String sqlMostrarPedidos = "SELECT id_pedido, total FROM Pedido WHERE id_cliente = ?";
                try (PreparedStatement stmtMostrarPedidos = conn.prepareStatement(sqlMostrarPedidos)) {
                    stmtMostrarPedidos.setInt(1, idCliente);
                    ResultSet rsPedidos = stmtMostrarPedidos.executeQuery();

                    if (!rsPedidos.isBeforeFirst()) {
                        System.out.println("No se encontraron pedidos para este cliente.");
                        return;
                    }

                    // Mostrar los pedidos
                    System.out.println("Pedidos de " + nombreCliente + ":");
                    while (rsPedidos.next()) {
                        int idPedido = rsPedidos.getInt("id_pedido");
                        double total = rsPedidos.getDouble("total");
                        System.out.println("ID Pedido: " + idPedido + " - Total: $" + total);
                    }

                    // Solicitar ID del pedido a eliminar
                    System.out.println("Ingrese el ID del pedido que desea eliminar: ");
                    int idPedidoEliminar = scanner.nextInt();

                    // Eliminar los detalles del pedido
                    try (PreparedStatement stmtEliminarDetalles = conn.prepareStatement(sqlEliminarDetalles)) {
                        stmtEliminarDetalles.setInt(1, idPedidoEliminar);
                        stmtEliminarDetalles.executeUpdate();  // Elimina los detalles del pedido
                    }

                    // Eliminar el pedido
                    try (PreparedStatement stmtEliminarPedido = conn.prepareStatement(sqlEliminarPedido)) {
                        stmtEliminarPedido.setInt(1, idCliente);
                        stmtEliminarPedido.setInt(2, idPedidoEliminar);
                        int filasAfectadas = stmtEliminarPedido.executeUpdate();

                        if (filasAfectadas > 0) {
                            System.out.println("Pedido eliminado correctamente.");
                        } else {
                            System.out.println("No se encontró el pedido con ese ID para este cliente.");
                        }
                    }
                }
            } else {
                System.out.println("Cliente no encontrado.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
