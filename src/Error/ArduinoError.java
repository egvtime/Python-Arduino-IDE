package Error;

public class ArduinoError {
    private final String text;
    private final Location<Integer, Integer> location;  // line, column
    private String solution;
    private final String errorType;
    private final boolean applicationError;

    //Code Error
    public ArduinoError(String text, Location<Integer, Integer> location, String errorType, String solution) {
        this.text = text;
        this.location = location;
        this.errorType = errorType;
        this.solution = solution;
        applicationError = false;
    }

    //Application Error
    public ArduinoError(String text, String errorType, String solution) {
        this.text = text;
        this.location = null;
        this.errorType = errorType;
        this.solution = solution;
        applicationError = true;
    }

    public Location<Integer, Integer> getLocation() {
        return location;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getText() {
        return text;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public boolean isApplicationError() {
        return applicationError;
    }

    @Override
    public String toString() {
        if(location == null) {
            if(applicationError) {
                return "Application Error Detected" + "\n" + "Error Type: " + getErrorType() + "\n\t" + getText() + "\n\t\t" + "Solution: " + getSolution() ;
            }else{
                return "Exception Detected" + "\n" + getText();
            }
        }
        return getErrorType() + " Detected at " + location + "\n\t" + getText() + "\n\t\t" + "Solution: " + getSolution();
    }

    public static class Location<L, C> {
        public final L line;
        public final C column;

        public Location(L line, C column) {
            this.line = line;
            this.column = column;
        }

        public L getLine() {
            return line;
        }

        public C getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return line + ";" + column;
        }
    }
}
