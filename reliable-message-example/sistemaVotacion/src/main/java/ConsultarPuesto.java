package sistemaVotacion;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsultarPuesto {

    private final ConexionBD conexion = new ConexionBD();

    private final Scanner scanner;

    public ConsultarPuesto() {
        Scanner tmp;
        try {
            tmp = new Scanner(System.in);
        } catch (Exception e) {
            tmp = null;
        }
        this.scanner = tmp;
    }

    public static void main(String[] args) {
        new ConsultarPuesto().run();
    }

    public void run() {
        boolean continuar = true;
        while (continuar) {
            String cedula = pedirCedula();
            mostrarPuesto(cedula);
            System.out.print("\n¿Desea consultar otra cedula? (S/N): ");
            String resp = leerLinea().trim();
            continuar = resp.equalsIgnoreCase("S");
        }
        System.out.println("Gracias por usar el sistema de consulta.\n");
    }

    private String pedirCedula() {
        String cedula;
        while (true) {
            System.out.print("Bienvenido al sistema de consulta de puesto de votacion");
            System.out.println("--------------------------------");
            System.out.print("Ingrese su cedula: ");
            cedula = leerLinea().trim();
            if (cedula.matches("\\d+")) {
                break;
            }
            System.out.println("La cedula debe ser numerica. Intente de nuevo.");
        }
        return cedula;
    }
    
    private void mostrarPuesto(String cedula) {
        String sql = """
                SELECT mv.consecutive            AS mesa,
                       d.nombre                 AS departamento,
                       m.nombre                 AS municipio,
                       pv.id                    AS puesto_id,
                       pv.nombre                AS puesto_nombre,
                       pv.direccion             AS direccion
                FROM   ciudadano c
                JOIN   mesa_votacion mv ON mv.id = c.mesa_id
                JOIN   puesto_votacion pv ON pv.id = mv.puesto_id
                JOIN   municipio m ON m.id = pv.municipio_id
                JOIN   departamento d ON d.id = m.departamento_id
                WHERE  c.documento = ?""";

        try {
            List<Map<String, Object>> rows = conexion.getInfoBDWithParams(sql, cedula);
            if (rows.isEmpty()) {
                System.out.println("No se encontró información para la cédula " + cedula);
            } else {
                Map<String, Object> row = rows.get(0);
                System.out.println("\n----- Información de votación -----");
                System.out.println("Nombre       : " + row.get("nombre"));
                System.out.println("Departamento : " + row.get("departamento"));
                System.out.println("Municipio    : " + row.get("municipio"));
                System.out.println("Puesto (ID)  : " + row.get("puesto_id") + " - " + row.get("puesto_nombre"));
                System.out.println("Dirección    : " + row.get("direccion"));
                System.out.println("Mesa         : " + row.get("mesa"));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener información: " + e.getMessage());
        }
    }

    private String leerLinea() {
        if (System.console() != null) {
            return System.console().readLine();
        }
        return scanner.nextLine();
    }
} 