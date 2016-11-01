package org.blockchainj.params.IoP;

import org.blockchainj.core.*;
import org.blockchainj.params.AbstractBlockchainNetParams;
import org.blockchainj.params.TestNet2Params;
import org.blockchainj.params.TestNet3Params;
import org.blockchainj.store.BlockStore;
import org.blockchainj.store.BlockStoreException;

import java.math.BigInteger;
import java.util.Date;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by rodrigo on 10/31/16.
 */
public class IoP_TestNet3Params extends AbstractBlockchainNetParams {


    public IoP_TestNet3Params() {
        super(SupportedBlockchain.INTERNET_OF_PEOPLE);
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
        id = NetworkParametersGetter.getID_TESTNET();
        // Genesis hash is 000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943
        packetMagic = 0xb1fc50b3;
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1d00ffffL);
        port = 7475;
        addressHeader = 130;
        p2shHeader = 49;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 76;
        genesisBlock.setTime(1463452342L);
        genesisBlock.setDifficultyTarget(0x1d00ffffL);
        genesisBlock.setNonce(3335213172L);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210000;
        // the amount of blocks premined that are taking into consideration when calculating the subsidy
        subsidyPremineDecreaseBlockCount = 42000;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("000000006f2bb863230cda4f4fbee520314077e599a90b9c6072ea2018d7f3a3"));
        alertSigningKey = Utils.HEX.decode("04302390343f91cc401d56d68b123028bf52e5fca1939df127f63c6467cdf9c8e2c14b61104cf817d0b780da337893ecc4aaff1309e536162dabbdb45200ca2b0a");

        dnsSeeds = new String[] {
                "ham4.fermat.cloud",
                "ham5.fermat.cloud",
                "ham6.fermat.cloud",
                "ham7.fermat.cloud",
                "ham8.fermat.cloud"
        };
        addrSeeds = null;
        bip32HeaderPub = 0xBB8F4852;
        bip32HeaderPriv = 0x2B7FA42A;

        majorityEnforceBlockUpgrade = TestNet2Params.TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = TestNet2Params.TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = TestNet2Params.TESTNET_MAJORITY_WINDOW;
    }

    private static IoP_TestNet3Params instance;
    public static synchronized IoP_TestNet3Params get() {
        if (instance == null) {
            instance = new IoP_TestNet3Params();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_TESTNET;
    }

    // February 16th 2012
    private static final Date testnetDiffDate = new Date(1329264000000L);

    @Override
    public void checkDifficultyTransitions(final StoredBlock storedPrev, final Block nextBlock,
                                           final BlockStore blockStore) throws VerificationException, BlockStoreException {
        if (!isDifficultyTransitionPoint(storedPrev) && nextBlock.getTime().after(testnetDiffDate)) {
            Block prev = storedPrev.getHeader();

            // After 15th February 2012 the rules on the testnet change to avoid people running up the difficulty
            // and then leaving, making it too hard to mine a block. On non-difficulty transition points, easy
            // blocks are allowed if there has been a span of 20 minutes without one.
            final long timeDelta = nextBlock.getTimeSeconds() - prev.getTimeSeconds();
            // There is an integer underflow bug in blockchain-qt that means mindiff blocks are accepted when time
            // goes backwards.
            if (timeDelta >= 0 && timeDelta <= NetworkParameters.TARGET_SPACING * 2) {
                // Walk backwards until we find a block that doesn't have the easiest proof of work, then check
                // that difficulty is equal to that one.
                StoredBlock cursor = storedPrev;
                while (!cursor.getHeader().equals(getGenesisBlock()) &&
                        cursor.getHeight() % getInterval() != 0 &&
                        cursor.getHeader().getDifficultyTargetAsInteger().equals(getMaxTarget()))
                    cursor = cursor.getPrev(blockStore);
                BigInteger cursorTarget = cursor.getHeader().getDifficultyTargetAsInteger();
                BigInteger newTarget = nextBlock.getDifficultyTargetAsInteger();
                if (!cursorTarget.equals(newTarget))
                    throw new VerificationException("Testnet block transition that is not allowed: " +
                            Long.toHexString(cursor.getHeader().getDifficultyTarget()) + " vs " +
                            Long.toHexString(nextBlock.getDifficultyTarget()));
            }
        } else {
            super.checkDifficultyTransitions(storedPrev, nextBlock, blockStore);
        }
    }
}
