package com.hermses.client;

public class ClientLauncher {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && ("--gui".equalsIgnoreCase(args[0]) || "gui".equalsIgnoreCase(args[0]))) {
            com.hermses.client.ui.ChatApp.main(trimFirst(args));
            return;
        }
        ClientMain.main(args);
    }

    private static String[] trimFirst(String[] in) {
        if (in.length <= 1) return new String[0];
        String[] out = new String[in.length-1];
        System.arraycopy(in,1,out,0,out.length);
        return out;
    }
}
