package xyz.stasiak.javamapreduce.wordcount;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

import xyz.stasiak.javamapreduce.processing.Controller;
import xyz.stasiak.javamapreduce.processing.Server;
import xyz.stasiak.javamapreduce.rmi.RmiUtil;
import xyz.stasiak.javamapreduce.util.LoggingUtil;
import xyz.stasiak.javamapreduce.util.SystemProperties;

public class Application {
    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());
    private Registry rmiRegistry;
    private Controller controller;
    private Server server;

    Application() {
        LoggingUtil.logInfo(LOGGER, Application.class, "Starting Java MapReduce application");
        initializeRmiRegistry();
        initializeRemoteObjects();
        registerShutdownHook();
    }

    private void initializeRmiRegistry() {
        try {
            var rmiPort = Integer.parseInt(SystemProperties.getRmiPort());
            rmiRegistry = LocateRegistry.createRegistry(rmiPort);
            LoggingUtil.logInfo(LOGGER, Application.class, "RMI Registry started on port " + rmiPort);
        } catch (RemoteException e) {
            try {
                LoggingUtil.logWarning(LOGGER, Application.class, "Registry already exists, attempting to locate it",
                        e);
                rmiRegistry = LocateRegistry.getRegistry();
                rmiRegistry.list();
                LoggingUtil.logInfo(LOGGER, Application.class, "Successfully connected to existing RMI Registry");
            } catch (RemoteException re) {
                LoggingUtil.logSevere(LOGGER, Application.class, "Failed to create or locate RMI registry", re);
                throw new IllegalStateException("Failed to create or locate RMI registry", re);
            }
        }
    }

    private void initializeRemoteObjects() {
        try {
            controller = new Controller();
            rmiRegistry.rebind("node", controller);
            server = new Server(controller);
            rmiRegistry.rebind("server", server);
        } catch (RemoteException e) {
            LoggingUtil.logSevere(LOGGER, Application.class, "Failed to initialize remote objects", e);
            throw new IllegalStateException("Could not initialize remote objects", e);
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                RmiUtil.shutdown();
                if (controller != null) {
                    controller.shutdownExecutor();
                    rmiRegistry.unbind("node");
                    LoggingUtil.logInfo(LOGGER, Application.class, "Controller unbound from registry");
                }
                if (server != null) {
                    rmiRegistry.unbind("server");
                    LoggingUtil.logInfo(LOGGER, Application.class, "Server unbound from registry");
                }
            } catch (Exception e) {
                LoggingUtil.logWarning(LOGGER, Application.class, "Error while cleaning up remote objects", e);
            }
        }));
    }

    public static void main(String[] args) {
        new Application();
    }
}
