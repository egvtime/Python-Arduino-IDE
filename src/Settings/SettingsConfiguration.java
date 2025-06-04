package Settings;

public class SettingsConfiguration {
    private boolean includeLibraryInConversion;
    private boolean DeveloperTools;

    public SettingsConfiguration(boolean includeLibraryInConversion, boolean developerTools) {
        this.includeLibraryInConversion = includeLibraryInConversion;
        DeveloperTools = developerTools;
    }

    public void setIncludeLibraryInConversion(boolean includeLibraryInConversion) {
        this.includeLibraryInConversion = includeLibraryInConversion;
    }

    public boolean getDisplayLibraryCheck() {
        return includeLibraryInConversion;
    }

    public boolean getDeveloperTools() {
        return DeveloperTools;
    }

    public void setDeveloperTools(boolean developerTools) {
        DeveloperTools = developerTools;
    }

    @Override
    public String toString() {
        return "SettingsConfiguration{" +
                "\nincludeLibraryInConversion=" + includeLibraryInConversion +
                ", \nDeveloperTools=" + DeveloperTools +
                "\n}";
    }
}
