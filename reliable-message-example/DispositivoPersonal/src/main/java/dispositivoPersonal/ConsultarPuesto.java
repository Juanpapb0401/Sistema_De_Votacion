package dispositivoPersonal;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsultarPuesto {

    private final ConexionBD conexion = new ConexionBD();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new ConsultarPuesto().run();
    }

    private void run() {
        boolean continuar = true;
        while (continuar) {
            String cedula = pedirCedula();
            mostrarPuesto(cedula);
            System.out.print("\n¿Desea consultar otra cédula? (S/N): ");
            continuar = scanner.nextLine().trim().equalsIgnoreCase("S");
        }
        System.out.println("Gracias por usar el sistema de consulta.");
    }

    private String pedirCedula() {
        String cedula;
        while (true) {
            System.out.println("Bienvenido al sistema de consulta de puesto de votacion");
            System.out.println("--------------------------------");
            System.out.print("Ingrese su cédula: ");
            cedula = scanner.nextLine().trim();
            if (cedula.matches("\\d+")) break;
            System.out.println("La cédula debe ser numérica. Intente de nuevo.");
        }
        return cedula;
    }

    private void mostrarPuesto(String cedula) {
        String sql = """
            SELECT mv.consecutive AS mesa, d.nombre AS departamento, m.nombre AS municipio,
                   pv.id AS puesto_id, pv.nombre AS puesto_nombre, pv.direccion AS direccion
            FROM ciudadano c
            JOIN mesa_votacion mv ON mv.id = c.mesa_id
            JOIN puesto_votacion pv ON pv.id = mv.puesto_id
            JOIN municipio m ON m.id = pv.municipio_id
            JOIN departamento d ON d.id = m.departamento_id
            WHERE c.documento = ?""";
        try {
            List<Map<String, Object>> rows = conexion.getInfoBDWithParams(sql, cedula);
            if (rows.isEmpty()) {
                System.out.println("No se encontró información para la cédula " + cedula);
            } else {
                Map<String, Object> r = rows.get(0);
                System.out.println("\n----- Información de votación -----");
                System.out.println("Departamento : " + r.get("departamento"));
                System.out.println("Municipio    : " + r.get("municipio"));
                System.out.println("Puesto (ID)  : " + r.get("puesto_id") + " - " + r.get("puesto_nombre"));
                System.out.println("Dirección    : " + r.get("direccion"));
                System.out.println("Mesa         : " + r.get("mesa"));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener información: " + e.getMessage());
        }
    }
} 