package sistemaVotacion;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;


public final class MesaConfig {

    private static final int mesaId;

    static {
        Integer tmp = null;

        String sys = System.getProperty("mesaId");
        if (sys != null && !sys.isBlank()) {
            try {
                tmp = Integer.parseInt(sys.trim());
            } catch (NumberFormatException ignored) {}
        }

        if (tmp == null) {
            Path p = Path.of("mesa.properties");
            if (Files.exists(p)) {
                Properties props = new Properties();
                try (FileInputStream in = new FileInputStream(p.toFile())) {
                    props.load(in);
                    String val = props.getProperty("mesaId");
                    if (val != null) {
                        tmp = Integer.parseInt(val.trim());
                    }
                } catch (IOException | NumberFormatException ignored) {}
            }
        }

        if (tmp == null) {
            System.err.println("[ERROR] No se ha definido 'mesaId'. Use -DmesaId=NUM o mesa.properties.");
            System.exit(1);
            tmp = -1;
        }

        mesaId = tmp;
        System.out.println("[INFO] Mesa de votación configurada con id=" + mesaId);
    }

    private MesaConfig() {}

    public static int getMesaId() {
        return mesaId;
    }
} 