public class PrinterI implements Demo.Printer
{
    public void printString(String message, com.zeroc.Ice.Current __current)
    {
        // Aquí se implementaría la lógica
        System.out.println("Mensaje recibido: " + message);
    }
}