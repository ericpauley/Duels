package zonedabone.Duels;

// Example plugin

// Imports for Register
import zonedabone.Duels.payment.Methods;

// Bukkit Imports
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

public class DuelsServerListener extends ServerListener {
    // Change "MyPlugin" to the name of your MAIN class file.
    // Let's say my plugins MAIN class is: Register.java
    // I would change "MyPlugin" to "Register"
    private Duels plugin;
    private Methods Methods = null;

    public DuelsServerListener(Duels plugin) {
        this.plugin = plugin;
        this.Methods = new Methods();
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        // Check to see if the plugin thats being disabled is the one we are using
        if (this.Methods != null && this.Methods.hasMethod()) {
            Boolean check = this.Methods.checkDisabled(event.getPlugin());

            if(check) {
                this.plugin.Method = null;
                System.out.println("[" + plugin.getDescription().getName() + "] Payment method was disabled. No longer accepting payments.");
            }
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        // Check to see if we need a payment method
        if (!this.Methods.hasMethod()) {
            if(this.Methods.setMethod(event.getPlugin())) {
                // You might want to make this a public variable inside your MAIN class public Method Method = null;
                // then reference it through this.plugin.Method so that way you can use it in the rest of your plugin ;)
                this.plugin.Method = this.Methods.getMethod();
                System.out.println("[" + plugin.getDescription().getName() + "] Payment method found (" + this.plugin.Method.getName() + " version: " + this.plugin.Method.getVersion() + ")");
            }
        }
    }
}