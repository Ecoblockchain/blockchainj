package org.blockchainj.core;

import org.blockchainj.net.discovery.DnsDiscovery;
import org.blockchainj.params.IoP.IoP_MainNetParams;
import org.blockchainj.params.IoP.IoP_RegTestParams;
import org.blockchainj.params.IoP.IoP_TestNet3Params;
import org.blockchainj.params.bitcoin.BTC_MainNetParams;
import org.blockchainj.params.bitcoin.BTC_RegTestParams;
import org.blockchainj.params.bitcoin.BTC_TestNet3Params;
import org.blockchainj.store.BlockStoreException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by rodrigo on 10/31/16.
 */
public class SupportedBlockchainTest {
    /**
     * Connects to bitcoin Main network using DNS discovery
     * @throws BlockStoreException
     */
    @Test
    public void connectToMainBitcoinTest() throws BlockStoreException {
        NetworkParameters networkParameters;
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.BITCOIN);
        networkParameters = BTC_MainNetParams.get();
        Context context = new Context(networkParameters);


        PeerGroup peerGroup = new PeerGroup(context);
        peerGroup.addPeerDiscovery(new DnsDiscovery(context.getParams()));
        peerGroup.start();

        Assert.assertTrue(peerGroup.isRunning());

    }

    /**
     * Connects to bitcoin Test network using DNS discovery
     * @throws BlockStoreException
     */
    @Test
    public void connectToTestNetBitcoinTest() throws BlockStoreException {
        NetworkParameters networkParameters;
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.BITCOIN);
        networkParameters = BTC_TestNet3Params.get();
        Context context = new Context(networkParameters);


        PeerGroup peerGroup = new PeerGroup(context);
        peerGroup.addPeerDiscovery(new DnsDiscovery(context.getParams()));
        peerGroup.start();

        Assert.assertTrue(peerGroup.isRunning());

    }

    /**
     * Connects to IoP Main network using DNS discovery
     * @throws BlockStoreException
     */
    @Test
    public void connectToMainIoPTest() throws BlockStoreException {
        NetworkParameters networkParameters;
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
        networkParameters = IoP_MainNetParams.get();
        Context context = new Context(networkParameters);

        PeerGroup peerGroup = new PeerGroup(context);
        peerGroup.addPeerDiscovery(new DnsDiscovery(context.getParams()));
        peerGroup.start();
        Assert.assertTrue(peerGroup.isRunning());
    }

    /**
     * Connects to bitcoin Main network using DNS discovery
     * @throws BlockStoreException
     */
    @Test
    public void connectToTestNetIoPTest() throws BlockStoreException {
        NetworkParameters networkParameters;
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
        networkParameters = IoP_TestNet3Params.get();
        Context context = new Context(networkParameters);


        PeerGroup peerGroup = new PeerGroup(context);
        peerGroup.addPeerDiscovery(new DnsDiscovery(context.getParams()));
        peerGroup.start();

        Assert.assertTrue(peerGroup.isRunning());

    }

    @Test
    public void getCorrectNetworkIdTest(){

        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
        NetworkParameters networkParameters = IoP_TestNet3Params.get();
        Context context = new Context(networkParameters);
        Assert.assertEquals("org.IoP.test", networkParameters.getId());

        networkParameters = IoP_MainNetParams.get();
        Assert.assertEquals("org.IoP.production", networkParameters.getId());

        networkParameters = IoP_RegTestParams.get();
        Assert.assertEquals("org.IoP.regtest", networkParameters.getId());


        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.BITCOIN);
        networkParameters = BTC_MainNetParams.get();
        Assert.assertEquals("org.bitcoin.production", networkParameters.getId());

        networkParameters = BTC_TestNet3Params.get();
        Assert.assertEquals("org.bitcoin.test", networkParameters.getId());

        networkParameters = BTC_RegTestParams.get();
        Assert.assertEquals("org.bitcoin.regtest", networkParameters.getId());

    }

    @Test
    public void createParamById(){
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
        NetworkParameters.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
        NetworkParameters params = NetworkParameters.fromID("org.IoP.test");
        Assert.assertEquals("org.IoP.test", params.getId());

        params = NetworkParameters.fromID("org.IoP.production");
        Assert.assertEquals("org.IoP.production", params.getId());

        params = NetworkParameters.fromID("org.IoP.regtest");
        Assert.assertEquals("org.IoP.regtest", params.getId());

        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.BITCOIN);
        NetworkParameters.setSupportedBlockchain(SupportedBlockchain.BITCOIN);

        params = NetworkParameters.fromID("org.bitcoin.test");
        Assert.assertEquals("org.bitcoin.test", params.getId());

        params = NetworkParameters.fromID("org.bitcoin.production");
        Assert.assertEquals("org.bitcoin.production", params.getId());

        params = NetworkParameters.fromID("org.bitcoin.regtest");
        Assert.assertEquals("org.bitcoin.regtest", params.getId());
    }

    @Test
    public void createParamByProtocolId(){
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
        NetworkParameters.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
        NetworkParameters params = NetworkParameters.fromPmtProtocolID("test");
        Assert.assertEquals("org.IoP.test", params.getId());

        params = NetworkParameters.fromPmtProtocolID("main");
        Assert.assertEquals("org.IoP.production", params.getId());

        params = NetworkParameters.fromPmtProtocolID("regtest");
        Assert.assertEquals("org.IoP.regtest", params.getId());

        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.BITCOIN);
        NetworkParameters.setSupportedBlockchain(SupportedBlockchain.BITCOIN);

        params = NetworkParameters.fromPmtProtocolID("test");
        Assert.assertEquals("org.bitcoin.test", params.getId());

        params = NetworkParameters.fromPmtProtocolID("main");
        Assert.assertEquals("org.bitcoin.production", params.getId());

        params = NetworkParameters.fromPmtProtocolID("regtest");
        Assert.assertEquals("org.bitcoin.regtest", params.getId());
    }
}
