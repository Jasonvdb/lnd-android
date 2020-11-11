package com.example.lnd_android;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lndmobile.Callback;
import lndmobile.Lndmobile;

public class Lightning {
    private static Lightning instance = null;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private Context context;

    public static Lightning getInstance() {
        if (instance == null) {
            instance = new Lightning();
        }

        return instance;
    }

    private void copyConfig() {
        AssetManager am = context.getAssets();

        try (InputStream in = am.open("lnd.conf")) {
            File conf = new File(context.getFilesDir(), "lnd.conf");
            copy(in, conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copy(InputStream in, File dst) throws IOException {
        try (OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    public void start() {
        copyConfig();

        final String args = "--lnddir=" + context.getFilesDir();

        class UnlockCallback implements Callback {
            @Override
            public void onError(Exception e) {
                System.out.println("UNLOCK ERROR");
                System.out.println(e.getMessage());
            }
            @Override
            public void onResponse(byte[] bytes) {
                System.out.println("READY TO BE UNLOCKED");
            }
        }

        class RPCCallback implements Callback {
            @Override
            public void onError(Exception e) {
                System.out.println("RPC ERROR");
                e.printStackTrace();
            }
            @Override
            public void onResponse(byte[] bytes) {
                System.out.println("RPC READY");

            }
        }

        Runnable startLnd = new Runnable() {
            @Override
            public void run() {
                Lndmobile.start(args, new UnlockCallback(), new RPCCallback());
            }
        };
        new Thread(startLnd).start();
    }

    public void createWallet() {
        class CreatedCallback implements Callback {
            @Override
            public void onError(Exception e) {
                System.out.println("INIT ERROR");
                e.printStackTrace();
            }
            @Override
            public void onResponse(byte[] bytes) {
                System.out.println("WALLET CREATED");
            }
        }

        Runnable initWallet = new Runnable() {
            @Override
            public void run() {
                String password = "Shhhhhhhh";

                System.out.println(password.getBytes());

                Lndmobile.initWallet(password.getBytes(), new CreatedCallback());
            }
        };
        new Thread(initWallet).start();
    }
}
