/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.blockchainj.core;

import com.google.common.base.Objects;
import org.blockchainj.net.discovery.*;
import org.blockchainj.params.*;
import org.blockchainj.params.IoP.*;
import org.blockchainj.params.bitcoin.*;
import org.blockchainj.script.*;
import org.blockchainj.store.BlockStore;
import org.blockchainj.store.BlockStoreException;

import org.blockchainj.utils.MonetaryFormat;

import javax.annotation.*;
import java.io.*;
import java.math.*;
import java.util.*;

import static org.blockchainj.core.Coin.*;
import static org.blockchainj.core.NetworkParametersGetter.*;
import static org.blockchainj.core.NetworkParametersGetter.getID_MAINNET;

import org.blockchainj.utils.VersionTally;

/**
 * <p>NetworkParameters contains the data needed for working with an instantiation of a Blockchain chain.</p>
 *
 * <p>This is an abstract class, concrete instantiations can be found in the params package. There are four:
 * one for the main network ({@link MainNetParams}), one for the public test network, and two others that are
 * intended for unit testing and local app development purposes. Although this class contains some aliases for
 * them, you are encouraged to call the static get() methods on each specific params class directly.</p>
 */
public abstract class NetworkParameters {

    /**
     * For static calls of the class, we need to define which network we are going to be connecting to.
     * For the common usage of NetworkParameters params = IoP_MainNetParams.get();
     * We first need to define the static call like NetworkParametersGetter.setSupportedBlockchain();
     */
    private static SupportedBlockchain staticSupportedBlockchain;
    public static void setSupportedBlockchain(SupportedBlockchain blockchain){
        staticSupportedBlockchain = blockchain;
        NetworkParametersGetter.setSupportedBlockchain(staticSupportedBlockchain);
    }

    /**
     * The alert signing key originally owned by Satoshi, and now passed on to Gavin along with a few others.
     * It will dinamically get the key from the NetworkParametersGetter class depending if it is Bitcoin or IoP
     */
    public static final byte[] SATOSHI_KEY = getSatoshiKey();

    /** The string returned by getId() for the main, production network where people trade things. */
    public static final String ID_MAINNET = getID_MAINNET();
    /** The string returned by getId() for the testnet. */
    public static final String ID_TESTNET = getID_TESTNET();
    /** The string returned by getId() for regtest mode. */
    public static final String ID_REGTEST = getID_REGTEST();
    /** Unit test network. */
    public static final String ID_UNITTESTNET = getID_UNITTEST();

    /** The string used by the payment protocol to represent the main net. */
    public static final String PAYMENT_PROTOCOL_ID_MAINNET = "main";
    /** The string used by the payment protocol to represent the test net. */
    public static final String PAYMENT_PROTOCOL_ID_TESTNET = "test";
    /** The string used by the payment protocol to represent unit testing (note that this is non-standard). */
    public static final String PAYMENT_PROTOCOL_ID_UNIT_TESTS = "unittest";
    public static final String PAYMENT_PROTOCOL_ID_REGTEST = "regtest";

    // TODO: Seed nodes should be here as well.

    protected Block genesisBlock;
    protected BigInteger maxTarget;
    protected int port;
    protected long packetMagic;  // Indicates message origin network and is used to seek to the next message when stream state is unknown.
    protected int addressHeader;
    protected int p2shHeader;
    protected int dumpedPrivateKeyHeader;
    protected int interval;
    protected int targetTimespan;
    protected byte[] alertSigningKey;
    protected int bip32HeaderPub;
    protected int bip32HeaderPriv;

    /** Used to check majorities for block version upgrade */
    protected int majorityEnforceBlockUpgrade;
    protected int majorityRejectBlockOutdated;
    protected int majorityWindow;

    // the premined amount of blocks that are taken into consideration for calculating the subsidy. Only for IoP
    protected int subsidyPremineDecreaseBlockCount = 0;

    protected int getSubsidyPremineDecreaseBlockCount(){
        return subsidyPremineDecreaseBlockCount;
    }

    // the supported blockchain of this network parameter
    protected SupportedBlockchain supportedBlockchain;

    /**
     * See getId(). This may be null for old deserialized wallets. In that case we derive it heuristically
     * by looking at the port number.
     */
    protected String id;

    /**
     * The depth of blocks required for a coinbase transaction to be spendable.
     */
    protected int spendableCoinbaseDepth;
    protected int subsidyDecreaseBlockCount;
    
    protected int[] acceptableAddressCodes;
    protected String[] dnsSeeds;
    protected int[] addrSeeds;
    protected HttpDiscovery.Details[] httpSeeds = {};
    protected Map<Integer, Sha256Hash> checkpoints = new HashMap<Integer, Sha256Hash>();
    protected transient MessageSerializer defaultSerializer = null;



    protected NetworkParameters(SupportedBlockchain supportedBlockchain) {
        this.supportedBlockchain = supportedBlockchain;
        NetworkParametersGetter.setSupportedBlockchain(supportedBlockchain);
        alertSigningKey = SATOSHI_KEY;
        genesisBlock = createGenesis(this);
    }

    protected SupportedBlockchain getSupportedBlockchain(){
        return this.supportedBlockchain;
    }

    private static Block createGenesis(NetworkParameters n) {
        Block genesisBlock = new Block(n, Block.BLOCK_VERSION_GENESIS);
        Transaction t = new Transaction(n);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"

            // the script of each supported blockchain is dinamically getted from the NetworkParametersGetter class
            byte[] bytes = getGenesisInput();
            t.addInput(new TransactionInput(n, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();

            // we also get the pubkeyScript dinamically depending on what network we are from the NetworkParametersGetter class.
            Script.writeBytes(scriptPubKeyBytes, getGenesisScriptPubKey());
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(n, t, FIFTY_COINS, scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
        genesisBlock.addTransaction(t);
        return genesisBlock;
    }

    public static final int TARGET_TIMESPAN = 14 * 24 * 60 * 60;  // 2 weeks per difficulty cycle, on average.
    public static final int TARGET_SPACING = 10 * 60;  // 10 minutes per block.
    public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;
    
    /**
     * Blocks with a timestamp after this should enforce BIP 16, aka "Pay to script hash". This BIP changed the
     * network rules in a soft-forking manner, that is, blocks that don't follow the rules are accepted but not
     * mined upon and thus will be quickly re-orged out as long as the majority are enforcing the rule.
     */
    public static final int BIP16_ENFORCE_TIME = 1333238400;
    
    /**
     * The maximum number of coins to be generated
     */
    public static final long MAX_COINS = 21000000;

    /**
     * The maximum money to be generated
     */
    public static final Coin MAX_MONEY = COIN.multiply(MAX_COINS);

    /** Alias for TestNet3Params.get(), use that instead. */
    @Deprecated
    public static NetworkParameters testNet() {
        if (staticSupportedBlockchain == SupportedBlockchain.BITCOIN)
            return BTC_TestNet3Params.get();
        else if (staticSupportedBlockchain == SupportedBlockchain.INTERNET_OF_PEOPLE)
            return IoP_TestNet3Params.get();

        return null;
    }

    /** Alias for TestNet2Params.get(), use that instead. */
    @Deprecated
    public static NetworkParameters testNet2() {

        if (staticSupportedBlockchain == SupportedBlockchain.BITCOIN)
            return BTC_TestNet2Params.get();
        else if (staticSupportedBlockchain == SupportedBlockchain.INTERNET_OF_PEOPLE)
            return IoP_TestNet2Params.get();

        return null;
    }

    /** Alias for TestNet3Params.get(), use that instead. */
    @Deprecated
    public static NetworkParameters testNet3() {
        if (staticSupportedBlockchain == SupportedBlockchain.BITCOIN)
            return BTC_TestNet3Params.get();
        else if (staticSupportedBlockchain == SupportedBlockchain.INTERNET_OF_PEOPLE)
            return IoP_TestNet3Params.get();

        return null;
    }

    /** Alias for MainNetParams.get(), use that instead */
    @Deprecated
    public static NetworkParameters prodNet() {
        if (staticSupportedBlockchain == SupportedBlockchain.BITCOIN)
            return BTC_MainNetParams.get();
        else if (staticSupportedBlockchain == SupportedBlockchain.INTERNET_OF_PEOPLE)
            return IoP_MainNetParams.get();

        return null;
    }

    /** Returns a testnet params modified to allow any difficulty target. */
    @Deprecated
    public static NetworkParameters unitTests() {
        if (staticSupportedBlockchain == SupportedBlockchain.BITCOIN)
            return BTC_UnitTestParams.get();
        else if (staticSupportedBlockchain == SupportedBlockchain.INTERNET_OF_PEOPLE)
            return IoP_UnitTestParams.get();

        return null;
    }

    /** Returns a standard regression test params (similar to unitTests) */
    @Deprecated
    public static NetworkParameters regTests() {
        if (staticSupportedBlockchain == SupportedBlockchain.BITCOIN)
            return BTC_RegTestParams.get();
        else if (staticSupportedBlockchain == SupportedBlockchain.INTERNET_OF_PEOPLE)
            return IoP_RegTestParams.get();

        return null;
    }

    /**
     * A Java package style string acting as unique ID for these parameters
     */
    public String getId() {
        return id;
    }

    public abstract String getPaymentProtocolId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return getId().equals(((NetworkParameters)o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /** Returns the network parameters for the given string ID or NULL if not recognized. */
    @Nullable
    public static NetworkParameters fromID(String id) {
        if (staticSupportedBlockchain == SupportedBlockchain.BITCOIN){
            if (id.equals(getID_MAINNET())) {
                return BTC_MainNetParams.get();
            } else if (id.equals(getID_TESTNET())) {
                return BTC_TestNet3Params.get();
            } else if (id.equals(getID_UNITTEST())) {
                return BTC_UnitTestParams.get();
            } else if (id.equals(getID_REGTEST())) {
                return BTC_RegTestParams.get();
            } else {
                return null;
            }
        }

        if (staticSupportedBlockchain == SupportedBlockchain.INTERNET_OF_PEOPLE){
            if (id.equals(getID_MAINNET())) {
                return IoP_MainNetParams.get();
            } else if (id.equals(getID_TESTNET())) {
                return IoP_TestNet3Params.get();
            } else if (id.equals(getID_UNITTEST())) {
                return IoP_UnitTestParams.get();
            } else if (id.equals(getID_REGTEST())) {
                return IoP_RegTestParams.get();
            } else {
                return null;
            }
        }

        return null;

    }

    /** Returns the network parameters for the given string paymentProtocolID or NULL if not recognized. */
    @Nullable
    public static NetworkParameters fromPmtProtocolID(String pmtProtocolId) {
        if (staticSupportedBlockchain == SupportedBlockchain.BITCOIN){
            if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_MAINNET)) {
                return BTC_MainNetParams.get();
            } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_TESTNET)) {
                return BTC_TestNet3Params.get();
            } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_UNIT_TESTS)) {
                return BTC_UnitTestParams.get();
            } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_REGTEST)) {
                return BTC_RegTestParams.get();
            } else {
                return null;
            }
        }

        if (staticSupportedBlockchain == SupportedBlockchain.INTERNET_OF_PEOPLE){
            if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_MAINNET)) {
                return IoP_MainNetParams.get();
            } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_TESTNET)) {
                return IoP_TestNet3Params.get();
            } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_UNIT_TESTS)) {
                return IoP_UnitTestParams.get();
            } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_REGTEST)) {
                return IoP_RegTestParams.get();
            } else {
                return null;
            }
        }
        return null;

    }

    public int getSpendableCoinbaseDepth() {
        return spendableCoinbaseDepth;
    }

    /**
     * Throws an exception if the block's difficulty is not correct.
     *
     * @throws VerificationException if the block's difficulty is not correct.
     */
    public abstract void checkDifficultyTransitions(StoredBlock storedPrev, Block next, final BlockStore blockStore) throws VerificationException, BlockStoreException;

    /**
     * Returns true if the block height is either not a checkpoint, or is a checkpoint and the hash matches.
     */
    public boolean passesCheckpoint(int height, Sha256Hash hash) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash == null || checkpointHash.equals(hash);
    }

    /**
     * Returns true if the given height has a recorded checkpoint.
     */
    public boolean isCheckpoint(int height) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash != null;
    }

    public int getSubsidyDecreaseBlockCount() {
        return subsidyDecreaseBlockCount;
    }

    /** Returns DNS names that when resolved, give IP addresses of active peers. */
    public String[] getDnsSeeds() {
        return dnsSeeds;
    }

    /** Returns IP address of active peers. */
    public int[] getAddrSeeds() {
        return addrSeeds;
    }

    /** Returns discovery objects for seeds implementing the Cartographer protocol. See {@link org.blockchainj.net.discovery.HttpDiscovery} for more info. */
    public HttpDiscovery.Details[] getHttpSeeds() {
        return httpSeeds;
    }

    /**
     * <p>Genesis block for this chain.</p>
     *
     * <p>The first block in every chain is a well known constant shared between all Blockchain implemenetations. For a
     * block to be valid, it must be eventually possible to work backwards to the genesis block by following the
     * prevBlockHash pointers in the block headers.</p>
     *
     * <p>The genesis blocks for both test and main networks contain the timestamp of when they were created,
     * and a message in the coinbase transaction. It says, <i>"The Times 03/Jan/2009 Chancellor on brink of second
     * bailout for banks"</i>.</p>
     */
    public Block getGenesisBlock() {
        return genesisBlock;
    }

    /** Default TCP port on which to connect to nodes. */
    public int getPort() {
        return port;
    }

    /** The header bytes that identify the start of a packet on this network. */
    public long getPacketMagic() {
        return packetMagic;
    }

    /**
     * First byte of a base58 encoded address. See {@link org.blockchainj.core.Address}. This is the same as acceptableAddressCodes[0] and
     * is the one used for "normal" addresses. Other types of address may be encountered with version codes found in
     * the acceptableAddressCodes array.
     */
    public int getAddressHeader() {
        return addressHeader;
    }

    /**
     * First byte of a base58 encoded P2SH address.  P2SH addresses are defined as part of BIP0013.
     */
    public int getP2SHHeader() {
        return p2shHeader;
    }

    /** First byte of a base58 encoded dumped private key. See {@link org.blockchainj.core.DumpedPrivateKey}. */
    public int getDumpedPrivateKeyHeader() {
        return dumpedPrivateKeyHeader;
    }

    /**
     * How much time in seconds is supposed to pass between "interval" blocks. If the actual elapsed time is
     * significantly different from this value, the network difficulty formula will produce a different value. Both
     * test and main Blockchain networks use 2 weeks (1209600 seconds).
     */
    public int getTargetTimespan() {
        return targetTimespan;
    }

    /**
     * The version codes that prefix addresses which are acceptable on this network. Although Satoshi intended these to
     * be used for "versioning", in fact they are today used to discriminate what kind of data is contained in the
     * address and to prevent accidentally sending coins across chains which would destroy them.
     */
    public int[] getAcceptableAddressCodes() {
        return acceptableAddressCodes;
    }

    /**
     * If we are running in testnet-in-a-box mode, we allow connections to nodes with 0 non-genesis blocks.
     */
    public boolean allowEmptyPeerChain() {
        return true;
    }

    /** How many blocks pass between difficulty adjustment periods. Blockchain standardises this to be 2015. */
    public int getInterval() {
        return interval;
    }

    /** Maximum target represents the easiest allowable proof of work. */
    public BigInteger getMaxTarget() {
        return maxTarget;
    }

    /**
     * The key used to sign {@link org.blockchainj.core.AlertMessage}s. You can use {@link org.blockchainj.core.ECKey#verify(byte[], byte[], byte[])} to verify
     * signatures using it.
     */
    public byte[] getAlertSigningKey() {
        return alertSigningKey;
    }

    /** Returns the 4 byte header for BIP32 (HD) wallet - public key part. */
    public int getBip32HeaderPub() {
        return bip32HeaderPub;
    }

    /** Returns the 4 byte header for BIP32 (HD) wallet - private key part. */
    public int getBip32HeaderPriv() {
        return bip32HeaderPriv;
    }

    /**
     * Returns the number of coins that will be produced in total, on this
     * network. Where not applicable, a very large number of coins is returned
     * instead (i.e. the main coin issue for Dogecoin).
     */
    public abstract Coin getMaxMoney();

    /**
     * Any standard (ie pay-to-address) output smaller than this value will
     * most likely be rejected by the network.
     */
    public abstract Coin getMinNonDustOutput();

    /**
     * The monetary object for this currency.
     */
    public abstract MonetaryFormat getMonetaryFormat();

    /**
     * Scheme part for URIs, for example "blockchain".
     */
    public abstract String getUriScheme();

    /**
     * Returns whether this network has a maximum number of coins (finite supply) or
     * not. Always returns true for Blockchain, but exists to be overriden for other
     * networks.
     */
    public abstract boolean hasMaxMoney();

    /**
     * Return the default serializer for this network. This is a shared serializer.
     * @return 
     */
    public final MessageSerializer getDefaultSerializer() {
        // Construct a default serializer if we don't have one
        if (null == this.defaultSerializer) {
            // Don't grab a lock unless we absolutely need it
            synchronized(this) {
                // Now we have a lock, double check there's still no serializer
                // and create one if so.
                if (null == this.defaultSerializer) {
                    // As the serializers are intended to be immutable, creating
                    // two due to a race condition should not be a problem, however
                    // to be safe we ensure only one exists for each network.
                    this.defaultSerializer = getSerializer(false);
                }
            }
        }
        return defaultSerializer;
    }

    /**
     * Construct and return a custom serializer.
     */
    public abstract BlockchainSerializer getSerializer(boolean parseRetain);

    /**
     * The number of blocks in the last  blocks
     * at which to trigger a notice to the user to upgrade their client, where
     * the client does not understand those blocks.
     */
    public int getMajorityEnforceBlockUpgrade() {
        return majorityEnforceBlockUpgrade;
    }

    /**
     * The number of blocks in the last blocks
     * at which to enforce the requirement that all new blocks are of the
     * newer type (i.e. outdated blocks are rejected).
     */
    public int getMajorityRejectBlockOutdated() {
        return majorityRejectBlockOutdated;
    }

    /**
     * The sampling window from which the version numbers of blocks are taken
     * in order to determine if a new block version is now the majority.
     */
    public int getMajorityWindow() {
        return majorityWindow;
    }

    /**
     * The flags indicating which block validation tests should be applied to
     * the given block. Enables support for alternative blockchains which enable
     * tests based on different criteria.
     * 
     * @param block block to determine flags for.
     * @param height height of the block, if known, null otherwise. Returned
     * tests should be a safe subset if block height is unknown.
     */
    public EnumSet<Block.VerifyFlag> getBlockVerificationFlags(final Block block,
            final VersionTally tally, final Integer height) {
        final EnumSet<Block.VerifyFlag> flags = EnumSet.noneOf(Block.VerifyFlag.class);

        if (block.isBIP34()) {
            final Integer count = tally.getCountAtOrAbove(Block.BLOCK_VERSION_BIP34);
            if (null != count && count >= getMajorityEnforceBlockUpgrade()) {
                flags.add(Block.VerifyFlag.HEIGHT_IN_COINBASE);
            }
        }
        return flags;
    }

    /**
     * The flags indicating which script validation tests should be applied to
     * the given transaction. Enables support for alternative blockchains which enable
     * tests based on different criteria.
     *
     * @param block block the transaction belongs to.
     * @param transaction to determine flags for.
     * @param height height of the block, if known, null otherwise. Returned
     * tests should be a safe subset if block height is unknown.
     */
    public EnumSet<Script.VerifyFlag> getTransactionVerificationFlags(final Block block,
            final Transaction transaction, final VersionTally tally, final Integer height) {
        final EnumSet<Script.VerifyFlag> verifyFlags = EnumSet.noneOf(Script.VerifyFlag.class);
        if (block.getTimeSeconds() >= NetworkParameters.BIP16_ENFORCE_TIME)
            verifyFlags.add(Script.VerifyFlag.P2SH);

        // Start enforcing CHECKLOCKTIMEVERIFY, (BIP65) for block.nVersion=4
        // blocks, when 75% of the network has upgraded:
        if (block.getVersion() >= Block.BLOCK_VERSION_BIP65 &&
            tally.getCountAtOrAbove(Block.BLOCK_VERSION_BIP65) > this.getMajorityEnforceBlockUpgrade()) {
            verifyFlags.add(Script.VerifyFlag.CHECKLOCKTIMEVERIFY);
        }

        return verifyFlags;
    }

    public abstract int getProtocolVersionNum(final ProtocolVersion version);

    public static enum ProtocolVersion {
        MINIMUM(70000),
        PONG(60001),
        BLOOM_FILTER(70000),
        CURRENT(70001);

        private final int blockchainProtocol;

        ProtocolVersion(final int blockchainProtocol) {
            this.blockchainProtocol = blockchainProtocol;
        }

        public int getBlockchainProtocolVersion() {
            return blockchainProtocol;
        }
    }
}
