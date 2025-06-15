package test;

import sistemaVotacion.VoteStationImpl;
import sistemaVotacion.ConexionBD;
import sistemaVotacion.MesaConfig;
import com.zeroc.Ice.Current;

/**
 * Implementación de VoteStationImpl para pruebas
 * Sobreescribe los métodos necesarios para usar las implementaciones falsas
 */
public class VoteStationTestImpl extends VoteStationImpl {
    
    private final int mesaActual;
    
    /**
     * Constructor que permite especificar la mesa actual para pruebas
     */
    public VoteStationTestImpl(int mesaId) {
        this.mesaActual = mesaId;
    }
    
    /**
     * Constructor por defecto (mesa 1)
     */
    public VoteStationTestImpl() {
        this(1);
    }
    
    /**
     * Sobreescribe el método vote para usar nuestras implementaciones falsas
     */
    @Override
    public int vote(String document, int candidateId, Current current) {
        try {
            // Usar la conexión BD falsa
            FakeConexionBD conexion = new FakeConexionBD();
            
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

            if (FakeVoteRegistry.hasVoted(document)) {
                System.out.println("Esta cédula ya registró su voto en esta mesa.");
                return 2; // Ya votó
            }

            // Registrar el voto
            FakeVoteRegistry.register(document);
            System.out.println("Voto registrado correctamente.");
            return 0;

        } catch (Exception e) {
            System.err.println("Error consultando la base de datos: " + e.getMessage());
            return 3; // En caso de error, asumimos que no está en la BD
        }
    }
} 