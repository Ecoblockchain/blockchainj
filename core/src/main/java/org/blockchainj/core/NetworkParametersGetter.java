package org.blockchainj.core;

/**
 * Created by rodrigo on 10/31/16.
 */
public final class NetworkParametersGetter {
    private static SupportedBlockchain supportedBlockchain;

    public static void setSupportedBlockchain (SupportedBlockchain blockchain){
        supportedBlockchain = blockchain;
    }

    public static byte[] getSatoshiKey(){
        switch (supportedBlockchain){
            case BITCOIN:
                return Utils.HEX.decode("04fc9702847840aaf195de8442ebecedf5b095cdbb9bc716bda9110971b28a49e0ead8564ff0db22209e0374782c093bb899692d524e9d6a6956e7c5ecbcd68284");
            case INTERNET_OF_PEOPLE:
                return Utils.HEX.decode("04db0f57cd33acb1bbef6088ace7cfd417d943936f9594eaa9d25e62e5af4a43ffb31830cbcc9c499b935e2961e3e77b5644cfbb316096d0d931b34427f8fab682");
            default:
                throw new RuntimeException("Blockchain not specified.");
        }
    }

    /**
     * Gets the id for a MainNet blockchain
     * @return a String representing the name of the mainnet network of the specified blockchain
     */
    public static String getID_MAINNET(){
        switch (supportedBlockchain){
            case BITCOIN:
                return "org.bitcoin.production";
            case INTERNET_OF_PEOPLE:
                return "org.IoP.production";
            default:
                throw new RuntimeException("Blockchain not specified.");
        }
    }

    /**
     * Gets the id for a MainNet blockchain
     * @return a String representing the name of the mainnet network of the specified blockchain
     */
    public static String getID_TESTNET(){
        switch (supportedBlockchain){
            case BITCOIN:
                return "org.bitcoin.test";
            case INTERNET_OF_PEOPLE:
                return "org.IoP.test";
            default:
                throw new RuntimeException("Blockchain not specified.");
        }
    }

    /**
     * Gets the id for a MainNet blockchain
     * @return a String representing the name of the mainnet network of the specified blockchain
     */
    public static String getID_REGTEST(){
        switch (supportedBlockchain){
            case BITCOIN:
                return "org.bitcoin.regtest";
            case INTERNET_OF_PEOPLE:
                return "org.IoP.regtest";
            default:
                throw new RuntimeException("Blockchain not specified.");
        }
    }

    /**
     * Gets the id for a MainNet blockchain
     * @return a String representing the name of the mainnet network of the specified blockchain
     */
    public static String getID_UNITTEST(){
        switch (supportedBlockchain){
            case BITCOIN:
                return "org.bitcoin.unittest";
            case INTERNET_OF_PEOPLE:
                return "org.IoP.unittest";
            default:
                throw new RuntimeException("Blockchain not specified.");
        }
    }

    public static byte[] getGenesisInput(){
        switch (supportedBlockchain){
            case BITCOIN:
                return Utils.HEX.decode
                        ("04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73");
            case INTERNET_OF_PEOPLE:
                return Utils.HEX.decode
                        ("04ffff001d0104364c61204e6163696f6e204d617920313674682032303136202d205361726d69656e746f2063657263612064656c2064657363656e736f");
            default:
                throw new RuntimeException("Blockchain not specified.");
        }
    }

    public static byte[] getGenesisScriptPubKey(){
        switch (supportedBlockchain){
            case BITCOIN:
                return Utils.HEX.decode
                        ("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f");
            case INTERNET_OF_PEOPLE:
                return Utils.HEX.decode
                        ("04ce49f9cdc8d23176c818fd7e27e7b614d128a47acfdad0e4542300e7efbd8879f1337af3188c0dcb0747fdf26d0cb3b0fca0f4e5d7aec53c43f4a933f570ae86");
            default:
                throw new RuntimeException("Blockchain not specified.");
        }
    }
}
