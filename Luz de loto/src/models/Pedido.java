package models;

import java.util.List;

public class Pedido {
    private String nombreCliente;
    private List<Platillo> platillos;
    private double total;

    public Pedido(String nombreCliente, List<Platillo> platillos, double total) {
        this.nombreCliente = nombreCliente;
        this.platillos = platillos;
        this.total = total;
    }

}


