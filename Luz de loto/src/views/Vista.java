package views;

import controllers.PedidoController;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Vista {
    private PedidoController pedidoController;

    public Vista() {
        this.pedidoController = new PedidoController();
    }

    public void mostrarMenu() {

        Scanner scanner = new Scanner(System.in);
        int opcion = -1;

        do {
            System.out.println("Menú:");
            System.out.println("1. Ver pedidos");
            System.out.println("2. Registrar pedido");
            System.out.println("3. Actualizar pedido");
            System.out.println("4. Eliminar pedido");
            System.out.println("5. Salir");

            try {
                System.out.print("Seleccione una opción: ");
                opcion = scanner.nextInt();

                switch (opcion) {
                    case 1:
                        pedidoController.listarPedidos();
                        break;
                    case 2:
                        registrarPedido();
                        break;
                    case 3:
                        actualizarPedido();
                        break;
                    case 4:
                        eliminarPedido();
                        break;
                    case 5:
                        System.out.println("Saliendo...");
                        break;
                    default:
                        System.out.println("Opción no válida. Inténtelo de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Valor erróneo, por favor ingrese un número.");
                scanner.nextLine(); // Limpiar el buffer del scanner
            }
        } while (opcion != 5);
    }

    private void registrarPedido() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese el nombre del cliente: ");
        String nombreCliente = scanner.nextLine();
        pedidoController.registrarPedido(nombreCliente);
    }

    private void actualizarPedido() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese el nombre del cliente cuyo pedido desea actualizar: ");
        String nombreCliente = scanner.nextLine();
        pedidoController.actualizarPedido(nombreCliente);
    }

    private void eliminarPedido() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese el nombre del cliente cuyo pedido desea eliminar: ");
        String nombreCliente = scanner.nextLine();
        pedidoController.eliminarPedido(nombreCliente);
    }
}
