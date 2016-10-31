/*
 * Copyright 2012, 2014 the original author or authors.
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

package org.blockchainj.uri;

import org.blockchainj.core.Address;
import org.blockchainj.params.MainNetParams;
import org.blockchainj.params.TestNet3Params;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.blockchainj.core.Coin.*;
import org.blockchainj.core.NetworkParameters;
import static org.junit.Assert.*;

public class BlockchainURITest {
    private BlockchainURI testObject = null;

    private static final NetworkParameters MAINNET = MainNetParams.get();
    private static final String MAINNET_GOOD_ADDRESS = "1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH";
    private static final String BLOCKCHAIN_SCHEME = MAINNET.getUriScheme();

    @Test
    public void testConvertToBlockchainURI() throws Exception {
        Address goodAddress = Address.fromBase58(MAINNET, MAINNET_GOOD_ADDRESS);
        
        // simple example
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello&message=AMessage", BlockchainURI.convertToBlockchainURI(goodAddress, parseCoin("12.34"), "Hello", "AMessage"));
        
        // example with spaces, ampersand and plus
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello%20World&message=Mess%20%26%20age%20%2B%20hope", BlockchainURI.convertToBlockchainURI(goodAddress, parseCoin("12.34"), "Hello World", "Mess & age + hope"));

        // no amount, label present, message present
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS + "?label=Hello&message=glory", BlockchainURI.convertToBlockchainURI(goodAddress, null, "Hello", "glory"));
        
        // amount present, no label, message present
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS + "?amount=0.1&message=glory", BlockchainURI.convertToBlockchainURI(goodAddress, parseCoin("0.1"), null, "glory"));
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS + "?amount=0.1&message=glory", BlockchainURI.convertToBlockchainURI(goodAddress, parseCoin("0.1"), "", "glory"));

        // amount present, label present, no message
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", BlockchainURI.convertToBlockchainURI(goodAddress, parseCoin("12.34"), "Hello", null));
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", BlockchainURI.convertToBlockchainURI(goodAddress, parseCoin("12.34"), "Hello", ""));
              
        // amount present, no label, no message
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS + "?amount=1000", BlockchainURI.convertToBlockchainURI(goodAddress, parseCoin("1000"), null, null));
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS + "?amount=1000", BlockchainURI.convertToBlockchainURI(goodAddress, parseCoin("1000"), "", ""));
        
        // no amount, label present, no message
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS + "?label=Hello", BlockchainURI.convertToBlockchainURI(goodAddress, null, "Hello", null));
        
        // no amount, no label, message present
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS + "?message=Agatha", BlockchainURI.convertToBlockchainURI(goodAddress, null, null, "Agatha"));
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS + "?message=Agatha", BlockchainURI.convertToBlockchainURI(goodAddress, null, "", "Agatha"));
      
        // no amount, no label, no message
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS, BlockchainURI.convertToBlockchainURI(goodAddress, null, null, null));
        assertEquals("blockchain:" + MAINNET_GOOD_ADDRESS, BlockchainURI.convertToBlockchainURI(goodAddress, null, "", ""));

        // different scheme
        final NetworkParameters alternativeParameters = new MainNetParams() {
            @Override
            public String getUriScheme() {
                return "test";
            }
        };

        assertEquals("test:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello&message=AMessage",
             BlockchainURI.convertToBlockchainURI(Address.fromBase58(alternativeParameters, MAINNET_GOOD_ADDRESS), parseCoin("12.34"), "Hello", "AMessage"));
    }

    @Test
    public void testGood_Simple() throws BlockchainURIParseException {
        testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS);
        assertNotNull(testObject);
        assertNull("Unexpected amount", testObject.getAmount());
        assertNull("Unexpected label", testObject.getLabel());
        assertEquals("Unexpected label", 20, testObject.getAddress().getHash160().length);
    }

    /**
     * Test a broken URI (bad scheme)
     */
    @Test
    public void testBad_Scheme() {
        try {
            testObject = new BlockchainURI(MAINNET, "blimpcoin:" + MAINNET_GOOD_ADDRESS);
            fail("Expecting BlockchainURIParseException");
        } catch (BlockchainURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad syntax)
     */
    @Test
    public void testBad_BadSyntax() {
        // Various illegal characters
        try {
            testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + "|" + MAINNET_GOOD_ADDRESS);
            fail("Expecting BlockchainURIParseException");
        } catch (BlockchainURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        try {
            testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "\\");
            fail("Expecting BlockchainURIParseException");
        } catch (BlockchainURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        // Separator without field
        try {
            testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":");
            fail("Expecting BlockchainURIParseException");
        } catch (BlockchainURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }
    }

    /**
     * Test a broken URI (missing address)
     */
    @Test
    public void testBad_Address() {
        try {
            testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME);
            fail("Expecting BlockchainURIParseException");
        } catch (BlockchainURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad address type)
     */
    @Test
    public void testBad_IncorrectAddressType() {
        try {
            testObject = new BlockchainURI(TestNet3Params.get(), BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS);
            fail("Expecting BlockchainURIParseException");
        } catch (BlockchainURIParseException e) {
            assertTrue(e.getMessage().contains("Bad address"));
        }
    }

    /**
     * Handles a simple amount
     * 
     * @throws BlockchainURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Amount() throws BlockchainURIParseException {
        // Test the decimal parsing
        testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210.12345678");
        assertEquals("654321012345678", testObject.getAmount().toString());

        // Test the decimal parsing
        testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=.12345678");
        assertEquals("12345678", testObject.getAmount().toString());

        // Test the integer parsing
        testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210");
        assertEquals("654321000000000", testObject.getAmount().toString());
    }

    /**
     * Handles a simple label
     * 
     * @throws BlockchainURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Label() throws BlockchainURIParseException {
        testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?label=Hello%20World");
        assertEquals("Hello World", testObject.getLabel());
    }

    /**
     * Handles a simple label with an embedded ampersand and plus
     * 
     * @throws BlockchainURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_LabelWithAmpersandAndPlus() throws BlockchainURIParseException {
        String testString = "Hello Earth & Mars + Venus";
        String encodedLabel = BlockchainURI.encodeURLString(testString);
        testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(testString, testObject.getLabel());
    }

    /**
     * Handles a Russian label (Unicode test)
     * 
     * @throws BlockchainURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_LabelWithRussian() throws BlockchainURIParseException {
        // Moscow in Russian in Cyrillic
        String moscowString = "\u041c\u043e\u0441\u043a\u0432\u0430";
        String encodedLabel = BlockchainURI.encodeURLString(moscowString); 
        testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(moscowString, testObject.getLabel());
    }

    /**
     * Handles a simple message
     * 
     * @throws BlockchainURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Message() throws BlockchainURIParseException {
        testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?message=Hello%20World");
        assertEquals("Hello World", testObject.getMessage());
    }

    /**
     * Handles various well-formed combinations
     * 
     * @throws BlockchainURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Combinations() throws BlockchainURIParseException {
        testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210&label=Hello%20World&message=Be%20well");
        assertEquals(
                "BlockchainURI['amount'='654321000000000','label'='Hello World','message'='Be well','address'='1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH']",
                testObject.toString());
    }

    /**
     * Handles a badly formatted amount field
     * 
     * @throws BlockchainURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Amount() throws BlockchainURIParseException {
        // Missing
        try {
            testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?amount=");
            fail("Expecting BlockchainURIParseException");
        } catch (BlockchainURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }

        // Non-decimal (BIP 21)
        try {
            testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?amount=12X4");
            fail("Expecting BlockchainURIParseException");
        } catch (BlockchainURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }
    }

    @Test
    public void testEmpty_Label() throws BlockchainURIParseException {
        assertNull(new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?label=").getLabel());
    }

    @Test
    public void testEmpty_Message() throws BlockchainURIParseException {
        assertNull(new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?message=").getMessage());
    }

    /**
     * Handles duplicated fields (sneaky address overwrite attack)
     * 
     * @throws BlockchainURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Duplicated() throws BlockchainURIParseException {
        try {
            testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?address=aardvark");
            fail("Expecting BlockchainURIParseException");
        } catch (BlockchainURIParseException e) {
            assertTrue(e.getMessage().contains("address"));
        }
    }

    @Test
    public void testGood_ManyEquals() throws BlockchainURIParseException {
        assertEquals("aardvark=zebra", new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":"
                + MAINNET_GOOD_ADDRESS + "?label=aardvark=zebra").getLabel());
    }
    
    /**
     * Handles unknown fields (required and not required)
     * 
     * @throws BlockchainURIParseException
     *             If something goes wrong
     */
    @Test
    public void testUnknown() throws BlockchainURIParseException {
        // Unknown not required field
        testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?aardvark=true");
        assertEquals("BlockchainURI['aardvark'='true','address'='1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH']", testObject.toString());

        assertEquals("true", testObject.getParameterByName("aardvark"));

        // Unknown not required field (isolated)
        try {
            testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?aardvark");
            fail("Expecting BlockchainURIParseException");
        } catch (BlockchainURIParseException e) {
            assertTrue(e.getMessage().contains("no separator"));
        }

        // Unknown and required field
        try {
            testObject = new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?req-aardvark=true");
            fail("Expecting BlockchainURIParseException");
        } catch (BlockchainURIParseException e) {
            assertTrue(e.getMessage().contains("req-aardvark"));
        }
    }

    @Test
    public void brokenURIs() throws BlockchainURIParseException {
        // Check we can parse the incorrectly formatted URIs produced by blockchain.info and its iPhone app.
        String str = "blockchain://1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH?amount=0.01000000";
        BlockchainURI uri = new BlockchainURI(str);
        assertEquals("1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH", uri.getAddress().toString());
        assertEquals(CENT, uri.getAmount());
    }

    @Test(expected = BlockchainURIParseException.class)
    public void testBad_AmountTooPrecise() throws BlockchainURIParseException {
        new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=0.123456789");
    }

    @Test(expected = BlockchainURIParseException.class)
    public void testBad_NegativeAmount() throws BlockchainURIParseException {
        new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=-1");
    }

    @Test(expected = BlockchainURIParseException.class)
    public void testBad_TooLargeAmount() throws BlockchainURIParseException {
        new BlockchainURI(MAINNET, BLOCKCHAIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=100000000");
    }

    @Test
    public void testPaymentProtocolReq() throws Exception {
        // Non-backwards compatible form ...
        BlockchainURI uri = new BlockchainURI(TestNet3Params.get(), "blockchain:?r=https%3A%2F%2Fblockchaincore.org%2F%7Egavin%2Ff.php%3Fh%3Db0f02e7cea67f168e25ec9b9f9d584f9");
        assertEquals("https://blockchaincore.org/~gavin/f.php?h=b0f02e7cea67f168e25ec9b9f9d584f9", uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of("https://blockchaincore.org/~gavin/f.php?h=b0f02e7cea67f168e25ec9b9f9d584f9"),
                uri.getPaymentRequestUrls());
        assertNull(uri.getAddress());
    }

    @Test
    public void testMultiplePaymentProtocolReq() throws Exception {
        BlockchainURI uri = new BlockchainURI(MAINNET,
                "blockchain:?r=https%3A%2F%2Fblockchaincore.org%2F%7Egavin&r1=bt:112233445566");
        assertEquals(ImmutableList.of("bt:112233445566", "https://blockchaincore.org/~gavin"), uri.getPaymentRequestUrls());
        assertEquals("https://blockchaincore.org/~gavin", uri.getPaymentRequestUrl());
    }

    @Test
    public void testNoPaymentProtocolReq() throws Exception {
        BlockchainURI uri = new BlockchainURI(MAINNET, "blockchain:" + MAINNET_GOOD_ADDRESS);
        assertNull(uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of(), uri.getPaymentRequestUrls());
        assertNotNull(uri.getAddress());
    }

    @Test
    public void testUnescapedPaymentProtocolReq() throws Exception {
        BlockchainURI uri = new BlockchainURI(TestNet3Params.get(),
                "blockchain:?r=https://merchant.com/pay.php?h%3D2a8628fc2fbe");
        assertEquals("https://merchant.com/pay.php?h=2a8628fc2fbe", uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of("https://merchant.com/pay.php?h=2a8628fc2fbe"), uri.getPaymentRequestUrls());
        assertNull(uri.getAddress());
    }
}
