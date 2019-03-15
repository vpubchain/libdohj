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

package org.bitcoinj.core;



import org.bitcoinj.params.UnitTestParams;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import org.bitcoinj.core.Utils;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;



public class SuperblockPartialMerkleTreeTest {

    private static final Logger log = LoggerFactory.getLogger(SuperblockPartialMerkleTreeTest.class);

    private static Map<String, Sha256Hash> allHashes = new HashMap<String, Sha256Hash>();

    @BeforeClass
    public static void beforeAll() throws Exception {
        for (int i = 0; i < 9; i++) {
            allHashes.put("" + i, Sha256Hash.of(new byte[]{(byte) i}));
        }
        allHashes.put("01", SuperblockPartialMerkleTree.combineLeftRight(allHashes.get("0").getBytes(), allHashes.get("1").getBytes()));
        allHashes.put("23", SuperblockPartialMerkleTree.combineLeftRight(allHashes.get("2").getBytes(), allHashes.get("3").getBytes()));
        allHashes.put("45", SuperblockPartialMerkleTree.combineLeftRight(allHashes.get("4").getBytes(), allHashes.get("5").getBytes()));
        allHashes.put("67", SuperblockPartialMerkleTree.combineLeftRight(allHashes.get("6").getBytes(), allHashes.get("7").getBytes()));

        allHashes.put("0123",SuperblockPartialMerkleTree.combineLeftRight(allHashes.get("01").getBytes(), allHashes.get("23").getBytes()));
        allHashes.put("4567",SuperblockPartialMerkleTree.combineLeftRight(allHashes.get("45").getBytes(), allHashes.get("67").getBytes()));

        allHashes.put("01234567",SuperblockPartialMerkleTree.combineLeftRight(allHashes.get("0123").getBytes(), allHashes.get("4567").getBytes()));

        allHashes.put("88",SuperblockPartialMerkleTree.combineLeftRight(allHashes.get("8").getBytes(), allHashes.get("8").getBytes()));
        allHashes.put("8888",SuperblockPartialMerkleTree.combineLeftRight(allHashes.get("88").getBytes(), allHashes.get("88").getBytes()));
        allHashes.put("88888888",SuperblockPartialMerkleTree.combineLeftRight(allHashes.get("8888").getBytes(), allHashes.get("8888").getBytes()));

    }


    @Test
    public void test1() throws Exception {
        NetworkParameters params = UnitTestParams.get();
        List<Sha256Hash> hashList = new ArrayList<Sha256Hash>();
        for (int i = 0; i < 8; i++) {
            hashList.add(allHashes.get(""+i));
        }
        byte[] includeBits = Utils.HEX.decode("ff");
        SuperblockPartialMerkleTree pmt = SuperblockPartialMerkleTree.buildFromLeaves(params, includeBits, hashList);
        List<Sha256Hash> out = new ArrayList<Sha256Hash>();
        //pmt.getTxnHashAndMerkleRoot(out);
        for (int i = 0; i < 8; i++) {
            assertEquals(i, pmt.getTransactionIndex(hashList.get(i)));
        }

        List<Sha256Hash> hash0Result = Arrays.asList(allHashes.get("1"), allHashes.get("23"), allHashes.get("4567"));
        assertEquals(hash0Result, pmt.getTransactionPath(allHashes.get("0")));
        List<Sha256Hash> hash1Result = Arrays.asList(allHashes.get("0"), allHashes.get("23"), allHashes.get("4567"));
        assertEquals(hash1Result, pmt.getTransactionPath(allHashes.get("1")));
        List<Sha256Hash> hash2Result = Arrays.asList(allHashes.get("3"), allHashes.get("01"), allHashes.get("4567"));
        assertEquals(hash2Result, pmt.getTransactionPath(allHashes.get("2")));
        List<Sha256Hash> hash3Result = Arrays.asList(allHashes.get("2"), allHashes.get("01"), allHashes.get("4567"));
        assertEquals(hash3Result, pmt.getTransactionPath(allHashes.get("3")));
        List<Sha256Hash> hash4Result = Arrays.asList(allHashes.get("5"), allHashes.get("67"), allHashes.get("0123"));
        assertEquals(hash4Result, pmt.getTransactionPath(allHashes.get("4")));
        List<Sha256Hash> hash5Result = Arrays.asList(allHashes.get("4"), allHashes.get("67"), allHashes.get("0123"));
        assertEquals(hash5Result, pmt.getTransactionPath(allHashes.get("5")));
        List<Sha256Hash> hash6Result = Arrays.asList(allHashes.get("7"), allHashes.get("45"), allHashes.get("0123"));
        assertEquals(hash6Result, pmt.getTransactionPath(allHashes.get("6")));
        List<Sha256Hash> hash7Result = Arrays.asList(allHashes.get("6"), allHashes.get("45"), allHashes.get("0123"));
        assertEquals(hash7Result, pmt.getTransactionPath(allHashes.get("7")));
    }

    @Test
    public void test2() throws Exception {
        NetworkParameters params = UnitTestParams.get();
        List<Sha256Hash> hashList = new ArrayList<Sha256Hash>();
        for (int i = 0; i < 8; i++) {
            hashList.add(allHashes.get(""+i));
        }
        byte[] includeBits = Utils.HEX.decode("0f");
        SuperblockPartialMerkleTree pmt = SuperblockPartialMerkleTree.buildFromLeaves(params, includeBits, hashList);
        for (int i = 0; i < 4; i++) {
            assertEquals(i, pmt.getTransactionIndex(hashList.get(i)));
        }
        for (int i = 4; i < 8; i++) {
            try {
                pmt.getTransactionIndex(hashList.get(i));
                fail("Expected VerificationException");
            } catch (VerificationException e) {
            }
        }


        List<Sha256Hash> hash0Result = Arrays.asList(allHashes.get("1"), allHashes.get("23"), allHashes.get("4567"));
        assertEquals(hash0Result, pmt.getTransactionPath(allHashes.get("0")));
        List<Sha256Hash> hash1Result = Arrays.asList(allHashes.get("0"), allHashes.get("23"), allHashes.get("4567"));
        assertEquals(hash1Result, pmt.getTransactionPath(allHashes.get("1")));
        List<Sha256Hash> hash2Result = Arrays.asList(allHashes.get("3"), allHashes.get("01"), allHashes.get("4567"));
        assertEquals(hash2Result, pmt.getTransactionPath(allHashes.get("2")));
        List<Sha256Hash> hash3Result = Arrays.asList(allHashes.get("2"), allHashes.get("01"), allHashes.get("4567"));
        assertEquals(hash3Result, pmt.getTransactionPath(allHashes.get("3")));

        for (int i = 4; i < 8; i++) {
            try {
                pmt.getTransactionPath(hashList.get(i));
                fail("Expected VerificationException");
            } catch (VerificationException e) {
            }
        }
    }

    @Test
    public void test2b() throws Exception {
        NetworkParameters params = UnitTestParams.get();
        List<Sha256Hash> hashList = new ArrayList<Sha256Hash>();
        for (int i = 0; i < 8; i++) {
            hashList.add(allHashes.get(""+i));
        }
        byte[] includeBits = Utils.HEX.decode("81");
        SuperblockPartialMerkleTree pmt = SuperblockPartialMerkleTree.buildFromLeaves(params, includeBits, hashList);
        for (int i = 1; i < 7; i++) {
            try {
                pmt.getTransactionIndex(hashList.get(i));
                fail("Expected VerificationException");
            } catch (VerificationException e) {
            }
        }
        assertEquals(0, pmt.getTransactionIndex(hashList.get(0)));
        assertEquals(7, pmt.getTransactionIndex(hashList.get(7)));

        for (int i = 1; i < 7; i++) {
            try {
                pmt.getTransactionPath(hashList.get(i));
                fail("Expected VerificationException");
            } catch (VerificationException e) {
            }
        }

        List<Sha256Hash> hash0Result = Arrays.asList(allHashes.get("1"), allHashes.get("23"), allHashes.get("4567"));
        assertEquals(hash0Result, pmt.getTransactionPath(allHashes.get("0")));
        List<Sha256Hash> hash7Result = Arrays.asList(allHashes.get("6"), allHashes.get("45"), allHashes.get("0123"));
        assertEquals(hash7Result, pmt.getTransactionPath(allHashes.get("7")));

    }

    @Test
    public void test3() throws Exception {
        NetworkParameters params = UnitTestParams.get();
        List<Sha256Hash> hashList = new ArrayList<Sha256Hash>();
        for (int i = 0; i < 9; i++) {
            hashList.add(allHashes.get(""+i));
        }
        byte[] includeBits = Utils.HEX.decode("ff01");
        SuperblockPartialMerkleTree pmt = SuperblockPartialMerkleTree.buildFromLeaves(params, includeBits, hashList);
        for (int i = 0; i < 9; i++) {
            assertEquals(i, pmt.getTransactionIndex(hashList.get(i)));
        }

        List<Sha256Hash> hash0Result = Arrays.asList(allHashes.get("1"), allHashes.get("23"), allHashes.get("4567"), allHashes.get("88888888"));
        assertEquals(hash0Result, pmt.getTransactionPath(allHashes.get("0")));
        List<Sha256Hash> hash1Result = Arrays.asList(allHashes.get("0"), allHashes.get("23"), allHashes.get("4567"), allHashes.get("88888888"));
        assertEquals(hash1Result, pmt.getTransactionPath(allHashes.get("1")));
        List<Sha256Hash> hash2Result = Arrays.asList(allHashes.get("3"), allHashes.get("01"), allHashes.get("4567"), allHashes.get("88888888"));
        assertEquals(hash2Result, pmt.getTransactionPath(allHashes.get("2")));
        List<Sha256Hash> hash3Result = Arrays.asList(allHashes.get("2"), allHashes.get("01"), allHashes.get("4567"), allHashes.get("88888888"));
        assertEquals(hash3Result, pmt.getTransactionPath(allHashes.get("3")));
        List<Sha256Hash> hash4Result = Arrays.asList(allHashes.get("5"), allHashes.get("67"), allHashes.get("0123"), allHashes.get("88888888"));
        assertEquals(hash4Result, pmt.getTransactionPath(allHashes.get("4")));
        List<Sha256Hash> hash5Result = Arrays.asList(allHashes.get("4"), allHashes.get("67"), allHashes.get("0123"), allHashes.get("88888888"));
        assertEquals(hash5Result, pmt.getTransactionPath(allHashes.get("5")));
        List<Sha256Hash> hash6Result = Arrays.asList(allHashes.get("7"), allHashes.get("45"), allHashes.get("0123"), allHashes.get("88888888"));
        assertEquals(hash6Result, pmt.getTransactionPath(allHashes.get("6")));
        List<Sha256Hash> hash7Result = Arrays.asList(allHashes.get("6"), allHashes.get("45"), allHashes.get("0123"), allHashes.get("88888888"));
        assertEquals(hash7Result, pmt.getTransactionPath(allHashes.get("7")));
        List<Sha256Hash> hash8Result = Arrays.asList(allHashes.get("8"), allHashes.get("88"), allHashes.get("8888"), allHashes.get("01234567"));
        assertEquals(hash8Result, pmt.getTransactionPath(allHashes.get("8")));

    }


}
