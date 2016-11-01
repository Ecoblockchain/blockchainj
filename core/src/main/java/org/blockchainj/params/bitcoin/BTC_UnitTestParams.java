package org.blockchainj.params.bitcoin;

import org.blockchainj.core.Block;
import org.blockchainj.core.NetworkParametersGetter;
import org.blockchainj.core.SupportedBlockchain;
import org.blockchainj.params.AbstractBlockchainNetParams;
import org.blockchainj.params.UnitTestParams;

import java.math.BigInteger;

/**
 * Created by rodrigo on 10/31/16.
 */
public class BTC_UnitTestParams extends AbstractBlockchainNetParams {

    public static final int UNITNET_MAJORITY_WINDOW = 8;
    public static final int TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED = 6;
    public static final int TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 4;

    public BTC_UnitTestParams() {
        super(SupportedBlockchain.BITCOIN);
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.BITCOIN);
        id = NetworkParametersGetter.getID_UNITTEST();
        packetMagic = 0x0b110907;
        addressHeader = 111;
        p2shHeader = 196;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        maxTarget = new BigInteger("00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
        genesisBlock.setTime(System.currentTimeMillis() / 1000);
        genesisBlock.setDifficultyTarget(Block.EASIEST_DIFFICULTY_TARGET);
        genesisBlock.solve();
        port = 18333;
        interval = 10;
        dumpedPrivateKeyHeader = 239;
        targetTimespan = 200000000;  // 6 years. Just a very big number.
        spendableCoinbaseDepth = 5;
        subsidyDecreaseBlockCount = 100;
        dnsSeeds = null;
        addrSeeds = null;
        bip32HeaderPub = 0x043587CF;
        bip32HeaderPriv = 0x04358394;

        majorityEnforceBlockUpgrade = 3;
        majorityRejectBlockOutdated = 4;
        majorityWindow = 7;
    }

    private static BTC_UnitTestParams instance;
    public static synchronized BTC_UnitTestParams get() {
        if (instance == null) {
            instance = new BTC_UnitTestParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return "btc_unittest";
    }
}
