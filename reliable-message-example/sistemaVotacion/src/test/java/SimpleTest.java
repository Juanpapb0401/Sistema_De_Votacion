package sistemaVotacion;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleTest {
    
    @Test
    public void testAlwaysPass() {
        assertTrue(true, "Este test siempre debe pasar");
        assertEquals(1, 1, "Los números deben ser iguales");
        assertNotNull(new Object(), "El objeto no debe ser nulo");
    }
} 