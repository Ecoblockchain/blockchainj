package org.blockchainj.params.IoP;

import org.blockchainj.core.Block;
import org.blockchainj.core.NetworkParametersGetter;
import org.blockchainj.core.SupportedBlockchain;
import org.blockchainj.params.MainNetParams;
import org.blockchainj.params.RegTestParams;

import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by rodrigo on 10/31/16.
 */
public class IoP_RegTestParams extends IoP_TestNet2Params {


    private static final BigInteger MAX_TARGET = new BigInteger("7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);

    public IoP_RegTestParams() {
        super();
        interval = 10000;
        maxTarget = MAX_TARGET;
        subsidyDecreaseBlockCount = 150;
        port = 14877;
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
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
                genesis.setNonce(2528424328L);
                genesis.setDifficultyTarget(0x207fFFFFL);
                genesis.setTime(1463452384L);
                checkState(genesis.getHashAsString().toLowerCase().equals("13ac5baa4b3656eec3ae4ab24b44ae602b9d1e549d9f1f238c1bfce54571b8b5"));
            }
            return genesis;
        }
    }

    private static IoP_RegTestParams instance;
    public static synchronized IoP_RegTestParams get() {
        if (instance == null) {
            instance = new IoP_RegTestParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_REGTEST;
    }
}
