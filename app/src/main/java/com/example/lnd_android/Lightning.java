package com.example.lnd_android;

import android.content.Context;
import android.content.res.AssetManager;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import lndmobile.Callback;
import lndmobile.Lndmobile;
import lnrpc.Walletunlocker;

public class Lightning {
    private static Lightning instance = null;
    private static String network = "testnet"; //TODO use config

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
                System.out.println("UNLOCK ERROR. WON'T BE ABLE TO UNLOCK.");
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
                String[] seed = {"about", "pride", "arrest", "ladder", "neutral", "unknown", "duck", "tilt", "electric", "cattle", "skate", "run", "friend", "advance", "melody", "helmet", "fat", "close", "believe", "copper", "unaware", "quote", "above", "decade"};

                Walletunlocker.InitWalletRequest.Builder initRequest = Walletunlocker.InitWalletRequest.newBuilder();
                initRequest.setWalletPassword(ByteString.copyFrom(password.getBytes()));
                initRequest.addAllCipherSeedMnemonic(Arrays.asList(seed));

                Lndmobile.initWallet(initRequest.build().toByteArray(), new CreatedCallback());
            }
        };
        new Thread(initWallet).start();
    }

    public void unlockWallet() {
        class UnlockCallback implements Callback {
            @Override
            public void onError(Exception e) {
                System.out.println("UNLOCK ERROR");
                e.printStackTrace();
            }
            @Override
            public void onResponse(byte[] bytes) {
                System.out.println("WALLET UNLOCKED");
            }
        }

        Runnable unlockWallet = new Runnable() {
            @Override
            public void run() {
                String password = "Shhhhhhhh";

                Walletunlocker.UnlockWalletRequest.Builder unlockRequest = Walletunlocker.UnlockWalletRequest.newBuilder();
                unlockRequest.setWalletPassword(ByteString.copyFrom(password.getBytes()));

                Lndmobile.unlockWallet(unlockRequest.build().toByteArray(), new UnlockCallback());
            }
        };
        new Thread(unlockWallet).start();
    }

    public boolean walletExists() {
        File directory = new File(context.getFilesDir().toString() + "/data/chain/bitcoin/" + network + "/wallet.db");
        return directory.exists();
    }
}
