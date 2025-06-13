package sistemaVotacion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

/**
 * Gestiona el archivo local donde se almacenan las cédulas que ya emitieron voto
 * en esta mesa. Se utiliza para impedir votos múltiples.
 */
public class VoteRegistry {

    private static final String FILE_NAME = "votantes.csv"; // una cédula por línea
    private static final Set<String> voted = new HashSet<>();

    static {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try {
                Files.lines(file.toPath())
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .forEach(voted::add);
            } catch (IOException e) {
                System.err.println("[WARN] No se pudo leer " + FILE_NAME + ": " + e.getMessage());
            }
        }
    }

    private VoteRegistry() {}

    public static boolean hasVoted(String cedula) {
        return voted.contains(cedula);
    }

    public static synchronized void register(String cedula) {
        if (voted.add(cedula)) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
                bw.write(cedula);
                bw.newLine();
            } catch (IOException e) {
                System.err.println("[ERROR] No se pudo actualizar " + FILE_NAME + ": " + e.getMessage());
            }
        }
    }
} 