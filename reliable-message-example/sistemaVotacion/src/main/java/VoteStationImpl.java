package sistemaVotacion;

import app.VoteStation;
import com.zeroc.Ice.Current;

public class VoteStationImpl implements VoteStation {
    
    @Override
    public int vote(String document, int candidateId, Current current) {
        ConexionBD conexion = new ConexionBD();
        int mesaActual = MesaConfig.getMesaId();

        try {
            java.util.List<java.util.Map<String, Object>> res =
                    conexion.getInfoBDWithParams("SELECT mesa_id FROM ciudadano WHERE documento = ?", document);

            if (res.isEmpty()) {
                System.out.println("La cédula no se encuentra registrada.");
                return 3; // No aparece en la BD
            }

            int mesaAsignada = ((Number) res.get(0).get("mesa_id")).intValue();
            if (mesaAsignada != mesaActual) {
                System.out.println("Esta cédula no está asignada a la mesa actual.");
                return 1; // No es su mesa
            }

            if (VoteRegistry.hasVoted(document)) {
                System.out.println("Esta cédula ya registró su voto en esta mesa.");
                return 2; // Ya votó
            }

            // Si llegamos aquí, puede votar
            return 0;

        } catch (Exception e) {
            System.err.println("Error consultando la base de datos: " + e.getMessage());
            return 3; // En caso de error, asumimos que no está en la BD
        }
    }
} 