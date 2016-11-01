package org.blockchainj.params.bitcoin;

import org.blockchainj.core.Block;
import org.blockchainj.core.NetworkParametersGetter;
import org.blockchainj.core.SupportedBlockchain;
import org.blockchainj.params.MainNetParams;
import org.blockchainj.params.RegTestParams;
import org.blockchainj.params.TestNet2Params;

import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by rodrigo on 10/31/16.
 */
public class BTC_RegTestParams extends BTC_TestNet2Params {

    private static final BigInteger MAX_TARGET = new BigInteger("7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);

    public BTC_RegTestParams() {
        super();
        interval = 10000;
        maxTarget = MAX_TARGET;
        subsidyDecreaseBlockCount = 150;
        port = 18444;
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.BITCOIN);
        id = NetworkParametersGetter.getID_REGTEST();

        majorityEnforceBlockUpgrade = MainNetParams.MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MainNetParams.MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MainNetParams.MAINNET_MAJORITY_WINDOW;
    }

    @Override
    public boolean allowEmptyPeerChain() {
        return true;
    }

    private static Block genesis;

    @Override
    public Block getGenesisBlock() {
        synchronized (RegTestParams.class) {
            if (genesis == null) {
                genesis = super.getGenesisBlock();
                genesis.setNonce(2);
                genesis.setDifficultyTarget(0x207fFFFFL);
                genesis.setTime(1296688602L);
                checkState(genesis.getHashAsString().toLowerCase().equals("0f9188f13cb7b2c71f2a335e3a4fc328bf5beb436012afca590b1a11466e2206"));
            }
            return genesis;
        }
    }

    private static BTC_RegTestParams instance;
    public static synchronized BTC_RegTestParams get() {
        if (instance == null) {
            instance = new BTC_RegTestParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_REGTEST;
    }
}
