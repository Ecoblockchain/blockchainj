package org.blockchainj.params.IoP;

import org.blockchainj.core.NetworkParametersGetter;
import org.blockchainj.core.SupportedBlockchain;
import org.blockchainj.core.Utils;
import org.blockchainj.params.AbstractBlockchainNetParams;
import org.blockchainj.params.TestNet2Params;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by rodrigo on 10/31/16.
 */
public class IoP_TestNet2Params extends AbstractBlockchainNetParams {

    public static final int TESTNET_MAJORITY_WINDOW = 100;
    public static final int TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED = 75;
    public static final int TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 51;

    public IoP_TestNet2Params() {
        super(SupportedBlockchain.INTERNET_OF_PEOPLE);
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
        id = NetworkParametersGetter.getID_TESTNET();
        packetMagic = 0x35b2cc9eL;
        port = 18444;
        addressHeader = 130;
        p2shHeader = 49;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1d0fffffL);
        dumpedPrivateKeyHeader = 76;
        genesisBlock.setTime(1463452384);
        genesisBlock.setDifficultyTarget(0x207fffff);
        genesisBlock.setNonce(2528424328L);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210000;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("13ac5baa4b3656eec3ae4ab24b44ae602b9d1e549d9f1f238c1bfce54571b8b5"));
        dnsSeeds = null;
        addrSeeds = null;
        bip32HeaderPub = 0xBB8F4852;
        bip32HeaderPriv = 0x2B7FA42A;

        majorityEnforceBlockUpgrade = TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = TESTNET_MAJORITY_WINDOW;
    }

    private static IoP_TestNet2Params instance;
    public static synchronized IoP_TestNet2Params get() {
        if (instance == null) {
            instance = new IoP_TestNet2Params();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return null;
    }
}
