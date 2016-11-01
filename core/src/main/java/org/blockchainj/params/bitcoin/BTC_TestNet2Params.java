package org.blockchainj.params.bitcoin;

import org.blockchainj.core.NetworkParametersGetter;
import org.blockchainj.core.SupportedBlockchain;
import org.blockchainj.core.Utils;
import org.blockchainj.params.AbstractBlockchainNetParams;
import org.blockchainj.params.TestNet2Params;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by rodrigo on 10/31/16.
 */
public class BTC_TestNet2Params extends AbstractBlockchainNetParams {

    public static final int TESTNET_MAJORITY_WINDOW = 100;
    public static final int TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED = 75;
    public static final int TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 51;

    public BTC_TestNet2Params() {
        super(SupportedBlockchain.BITCOIN);
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.BITCOIN);
        id = NetworkParametersGetter.getID_TESTNET();
        packetMagic = 0xfabfb5daL;
        port = 18333;
        addressHeader = 111;
        p2shHeader = 196;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1d0fffffL);
        dumpedPrivateKeyHeader = 239;
        genesisBlock.setTime(1296688602L);
        genesisBlock.setDifficultyTarget(0x1d07fff8L);
        genesisBlock.setNonce(384568319);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210000;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("00000007199508e34a9ff81e6ec0c477a4cccff2a4767a8eee39c11db367b008"));
        dnsSeeds = null;
        addrSeeds = null;
        bip32HeaderPub = 0x043587CF;
        bip32HeaderPriv = 0x04358394;

        majorityEnforceBlockUpgrade = TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = TESTNET_MAJORITY_WINDOW;
    }

    private static BTC_TestNet2Params instance;
    public static synchronized BTC_TestNet2Params get() {
        if (instance == null) {
            instance = new BTC_TestNet2Params();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return null;
    }
}
