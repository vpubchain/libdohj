/*
 * Copyright 2013 Google Inc.
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

package org.libdohj.params;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

import static com.google.common.base.Preconditions.checkState;
import java.math.BigInteger;
/**
 * Parameters for the main Syscoin production network on which people trade
 * goods and services.
 */
public class SyscoinMainNetParams extends AbstractSyscoinParams {
    public static final int MAINNET_MAJORITY_WINDOW = 2000;
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 1900;
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 1500;

    public SyscoinMainNetParams() {
        super();
        maxTarget = Utils.decodeCompactBits(0x1e0fffffL);


        dumpedPrivateKeyHeader = 128; //This is always addressHeader + 128
        addressHeader = 63;
        p2shHeader = 5;

        port = 8369;
        packetMagic = 0xcee2caff;


        segwitAddressHrp = "sys";
        bip32HeaderP2PKHpub = 0x0488b21e; // The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderP2PKHpriv = 0x0488ade4; // The 4 byte header that serializes in base58 to "xprv"
        bip32HeaderP2WPKHpub = 0x04b24746; // The 4 byte header that serializes in base58 to "zpub".
        bip32HeaderP2WPKHpriv = 0x04b2430c; // The 4 byte header that serializes in base58 to "zprv"

        genesisBlock.setDifficultyTarget(0x1e0fffffL);
        genesisBlock.setTime(1559520000L);
        genesisBlock.setNonce(1372898L);
        id = ID_SYSCOIN_MAINNET;
        subsidyDecreaseBlockCount = 100000;
        spendableCoinbaseDepth = 100;

        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("0000022642db0346b6e01c2a397471f4f12e65d4f4251ec96c1f85367a61a7ab"),
                genesisHash);

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        // This contains (at a minimum) the blocks which are not BIP30 compliant. BIP30 changed how duplicate
        // transactions are handled. Duplicated transactions could occur in the case where a coinbase had the same
        // extraNonce and the same outputs but appeared at different heights, and greatly complicated re-org handling.
        // Having these here simplifies block connection logic considerably.
        checkpoints.put(    0, Sha256Hash.wrap("0000022642db0346b6e01c2a397471f4f12e65d4f4251ec96c1f85367a61a7ab"));
        checkpoints.put(    250, Sha256Hash.wrap("00000c9ec0f9d60ce297bf9f9cbe1f2eb39165a0d3f69c1c55fc3f6680fe45c8"));
        checkpoints.put(    5000, Sha256Hash.wrap("eef3554a3f467bcdc7570f799cecdb262058cecf34d555827c99b5719b1df4f6"));
        checkpoints.put(    10000, Sha256Hash.wrap("e44257e8e027e8a67fd647c54e1bd6976988d75b416affabe3f82fd87a67f5ff"));
        checkpoints.put(    40000, Sha256Hash.wrap("4ad1ec207d62fa91485335feaf890150a0f4cf48c39b11e3dbfc22bdecc29dbc"));
        checkpoints.put(    100000, Sha256Hash.wrap("a54904302fd6fd0ee561cb894f15ad8c21c2601b305ffa9e15ef00df1c50db16"));
        checkpoints.put(    150000, Sha256Hash.wrap("73850eb99a6c32b4bfd67a26a7466ce3d0b4412d4174590c501e567c99f038fd"));
        checkpoints.put(    200000, Sha256Hash.wrap("a28fe36c63acb38065dadf09d74de5fdc1dac6433c204b215b37bab312dfab0d"));
        checkpoints.put(    240000, Sha256Hash.wrap("906918ba0cbfbd6e4e4e00d7d47d08bef3e409f47b59cb5bd3303f5276b88f0f"));
        checkpoints.put(    280000, Sha256Hash.wrap("651375427865345d37a090ca561c1ed135c6b8dafa591a59f2abf1eb26dfd538"));
        checkpoints.put(    292956, Sha256Hash.wrap("ae6dca1b9dd7adcb8a11c8ea7f9fe72bb47ff6e4156e1d172e2a8612b18a319d"));
        checkpoints.put(    350000, Sha256Hash.wrap("02501c7feba858c83e005acbf0505a892081288dcf7a8a37bd4fc47d7c24c799"));
        checkpoints.put(    390000, Sha256Hash.wrap("8654451a7ed5286ba5c830cdf6e65cbbd7a77f650216541bfbe50af04933741b"));
        checkpoints.put(    391285, Sha256Hash.wrap("76d13e8f08c2b7027251484078f734f91c485727031be6b4c21c42d5e103d0ad"));

        dnsSeeds = new String[] {
                "seed1.syscoin.org",
                "seed2.syscoin.org",
                "seed3.syscoin.org",
                "seed4.syscoin.org"
        };
    }

    private static SyscoinMainNetParams instance;
    public static synchronized SyscoinMainNetParams get() {
        if (instance == null) {
            instance = new SyscoinMainNetParams();
        }
        return instance;
    }


    @Override
    public String getPaymentProtocolId() {
        // TODO: CHANGE THIS
        return ID_SYSCOIN_MAINNET;
    }

    @Override
    public boolean isTestNet() {
        return false;
    }
}
