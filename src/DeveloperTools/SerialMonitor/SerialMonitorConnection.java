package DeveloperTools.SerialMonitor;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

public class SerialMonitorConnection {
    private SerialPort serialPort;
    private Scanner input;
    private OutputStream output;

    public boolean open(String portDescriptor, int baudRate) {
        serialPort = SerialPort.getCommPort(portDescriptor);
        serialPort.setBaudRate(baudRate);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (serialPort.openPort()) {
            input = new Scanner(serialPort.getInputStream());
            output = serialPort.getOutputStream();
            return true;
        }
        return false;
    }

    public String readLine() {
        if (input != null && input.hasNextLine()) {
            return input.nextLine();
        }
        return null;
    }

    public void write(String data) throws IOException {
        if (output != null) {
            output.write((data + "\n").getBytes());
            output.flush();
        }
    }

    public void close() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (serialPort != null) serialPort.closePort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] getPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] names = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            names[i] = ports[i].getSystemPortName();
        }
        return names;
    }
}
